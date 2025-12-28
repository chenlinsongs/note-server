package com.notes.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import com.notes.dto.request.NoteRequest;
import com.notes.dto.response.NoteDTO;
import com.notes.dto.response.NoteListDTO;
import com.notes.dto.response.NoteVersionDTO;
import com.notes.dto.response.TagDTO;
import com.notes.entity.Note;
import com.notes.entity.NoteTag;
import com.notes.entity.NoteVersion;
import com.notes.entity.Tag;
import com.notes.exception.ResourceNotFoundException;
import com.notes.repository.*;
import com.notes.service.NoteService;
import com.notes.service.SearchService;
import com.notes.util.ContentExtractor;
import com.notes.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {
    
    private static final int SNAPSHOT_INTERVAL = 10;  // 每10个版本保存一次快照
    
    private final NoteRepository noteRepository;
    private final NoteVersionRepository noteVersionRepository;
    private final NoteTagRepository noteTagRepository;
    private final TagRepository tagRepository;
    private final ContentExtractor contentExtractor;
    private final ObjectMapper objectMapper;
    private final SearchService searchService;
    
    @Override
    public Page<NoteListDTO> getNotes(String folderUid, Pageable pageable) {
        Page<Note> notes;
        if (folderUid != null && !folderUid.isEmpty()) {
            notes = noteRepository.findByFolderUidAndDeletedFalseOrderByIsPinnedDescUpdatedAtDesc(folderUid, pageable);
        } else {
            notes = noteRepository.findByDeletedFalseOrderByIsPinnedDescUpdatedAtDesc(pageable);
        }
        return notes.map(this::convertToListDTO);
    }
    
    @Override
    public NoteDTO getNote(String uid) {
        Note note = noteRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("笔记", "uid", uid));
        return convertToDTO(note);
    }
    
    private static final String DEFAULT_TITLE = "未命名文档";
    
    @Override
    @Transactional
    public NoteDTO createNote(NoteRequest request) {
        Note note = new Note();
        note.setUid(UidGenerator.generateNoteUid());
        // 标题为空时使用默认标题
        String title = request.getTitle();
        note.setTitle(title != null && !title.trim().isEmpty() ? title : DEFAULT_TITLE);
        note.setContent(request.getContent());
        note.setFolderUid(request.getFolderUid());
        note.setIsPinned(request.getIsPinned() != null && request.getIsPinned());
        
        // 提取纯文本和字数
        String contentText = contentExtractor.extractText(request.getContent());
        note.setContentText(contentText);
        note.setWordCount(contentExtractor.countWords(contentText));
        
        note = noteRepository.save(note);
        
        // 保存标签关联
        if (request.getTagUids() != null) {
            saveNoteTags(note.getUid(), request.getTagUids());
        }
        
        // 保存初始版本（快照）
        saveVersion(note, true);
        
        // 索引到 ES
        try {
            searchService.indexNote(note.getUid());
        } catch (Exception e) {
            log.warn("索引笔记失败: {}", e.getMessage());
        }
        
        return convertToDTO(note);
    }
    
    @Override
    @Transactional
    public NoteDTO updateNote(String uid, NoteRequest request) {
        Note note = noteRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("笔记", "uid", uid));
        
        String oldContent = note.getContent();
        String newContent = request.getContent();
        
        // 检查内容是否有变化
        boolean contentChanged = hasContentChanged(oldContent, newContent);
        
        // 标题为空时使用默认标题
        String title = request.getTitle();
        note.setTitle(title != null && !title.trim().isEmpty() ? title : DEFAULT_TITLE);
        note.setContent(newContent);
        if (request.getFolderUid() != null) {
            note.setFolderUid(request.getFolderUid());
        }
        if (request.getIsPinned() != null) {
            note.setIsPinned(request.getIsPinned());
        }
        
        // 提取纯文本和字数
        String contentText = contentExtractor.extractText(newContent);
        note.setContentText(contentText);
        note.setWordCount(contentExtractor.countWords(contentText));
        
        // 只有内容变化时才增加版本号并保存版本记录
        if (contentChanged) {
            note.setVersion(note.getVersion() + 1);
            note = noteRepository.save(note);
            
            // 保存版本（增量或快照）
            boolean isSnapshot = note.getVersion() % SNAPSHOT_INTERVAL == 0 || note.getVersion() == 1;
            saveVersionWithDiff(note, oldContent, isSnapshot);
            log.debug("内容变化，保存版本 {}", note.getVersion());
        } else {
            // 内容没变化，只更新其他字段（标题、标签等）
            note = noteRepository.save(note);
            log.debug("内容无变化，不创建新版本");
        }
        
        // 更新标签关联
        if (request.getTagUids() != null) {
            noteTagRepository.deleteByNoteUid(uid);
            saveNoteTags(uid, request.getTagUids());
        }
        
        // 更新 ES 索引
        try {
            searchService.indexNote(note.getUid());
        } catch (Exception e) {
            log.warn("更新索引失败: {}", e.getMessage());
        }
        
        return convertToDTO(note);
    }
    
    /**
     * 检查内容是否有变化
     */
    private boolean hasContentChanged(String oldContent, String newContent) {
        // 都为空，无变化
        if ((oldContent == null || oldContent.isEmpty()) && 
            (newContent == null || newContent.isEmpty())) {
            return false;
        }
        // 一个为空一个不为空，有变化
        if (oldContent == null || newContent == null) {
            return true;
        }
        // 比较 JSON 内容
        try {
            JsonNode oldNode = objectMapper.readTree(oldContent);
            JsonNode newNode = objectMapper.readTree(newContent);
            return !oldNode.equals(newNode);
        } catch (Exception e) {
            // JSON 解析失败，直接比较字符串
            return !oldContent.equals(newContent);
        }
    }
    
    @Override
    @Transactional
    public void deleteNote(String uid) {
        Note note = noteRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("笔记", "uid", uid));
        
        note.setDeleted(true);
        note.setDeletedAt(LocalDateTime.now());
        noteRepository.save(note);
        
        // 删除 ES 索引
        try {
            searchService.deleteNoteIndex(uid);
        } catch (Exception e) {
            log.warn("删除索引失败: {}", e.getMessage());
        }
    }
    
    @Override
    public List<NoteVersionDTO> getVersions(String uid) {
        if (!noteRepository.existsByUidAndDeletedFalse(uid)) {
            throw new ResourceNotFoundException("笔记", "uid", uid);
        }
        
        List<NoteVersion> versions = noteVersionRepository.findByNoteUidOrderByVersionDesc(uid);
        return versions.stream()
                .map(this::convertVersionToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public NoteDTO getVersion(String uid, Integer version) {
        Note note = noteRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("笔记", "uid", uid));
        
        String content = restoreContentToVersion(uid, version);
        
        NoteDTO dto = convertToDTO(note);
        dto.setContent(content);
        dto.setVersion(version);
        return dto;
    }
    
    @Override
    @Transactional
    public NoteDTO restoreVersion(String uid, Integer version) {
        Note note = noteRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("笔记", "uid", uid));
        
        String restoredContent = restoreContentToVersion(uid, version);
        
        note.setContent(restoredContent);
        String contentText = contentExtractor.extractText(restoredContent);
        note.setContentText(contentText);
        note.setWordCount(contentExtractor.countWords(contentText));
        note.setVersion(note.getVersion() + 1);
        
        note = noteRepository.save(note);
        
        // 保存恢复版本
        saveVersion(note, true);
        
        // 更新索引
        try {
            searchService.indexNote(uid);
        } catch (Exception e) {
            log.warn("更新索引失败: {}", e.getMessage());
        }
        
        return convertToDTO(note);
    }
    
    private void saveNoteTags(String noteUid, List<String> tagUids) {
        for (String tagUid : tagUids) {
            if (tagRepository.findByUidAndDeletedFalse(tagUid).isPresent()) {
                NoteTag noteTag = new NoteTag();
                noteTag.setNoteUid(noteUid);
                noteTag.setTagUid(tagUid);
                noteTagRepository.save(noteTag);
            }
        }
    }
    
    private void saveVersion(Note note, boolean isSnapshot) {
        NoteVersion version = new NoteVersion();
        version.setUid(UidGenerator.generateNoteVersionUid());
        version.setNoteUid(note.getUid());
        version.setVersion(note.getVersion());
        version.setIsSnapshot(isSnapshot);
        
        if (isSnapshot) {
            // MySQL JSON 类型不接受空字符串，使用空 JSON 对象
            String content = note.getContent();
            version.setSnapshotData(isValidJson(content) ? content : "{}");
        }
        
        version.setChangeSummary("版本 " + note.getVersion());
        noteVersionRepository.save(version);
    }
    
    /**
     * 检查字符串是否是有效的 JSON
     */
    private boolean isValidJson(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 保存版本（增量 patch 或 快照）
     * 
     * 存储策略：
     * - 版本1、11、21... (版本号 % 10 == 1 或 版本1): 保存完整快照
     * - 其他版本: 只保存与上一版本的差异 (JSON Patch)
     * 
     * 这样可以节省存储空间，同时保证恢复任意版本最多只需应用9个patch
     */
    private void saveVersionWithDiff(Note note, String oldContent, boolean isSnapshot) {
        NoteVersion version = new NoteVersion();
        version.setUid(UidGenerator.generateNoteVersionUid());
        version.setNoteUid(note.getUid());
        version.setVersion(note.getVersion());
        version.setIsSnapshot(isSnapshot);
        
        if (isSnapshot) {
            // 快照：保存完整内容（MySQL JSON 类型不接受空字符串）
            String content = note.getContent();
            version.setSnapshotData(isValidJson(content) ? content : "{}");
            log.debug("保存快照版本 {}", note.getVersion());
        } else {
            // 增量：只保存 patch
            try {
                String oldJson = isValidJson(oldContent) ? oldContent : "{}";
                String newJson = isValidJson(note.getContent()) ? note.getContent() : "{}";
                
                JsonNode oldNode = objectMapper.readTree(oldJson);
                JsonNode newNode = objectMapper.readTree(newJson);
                JsonNode patch = JsonDiff.asJson(oldNode, newNode);
                
                String patchStr = objectMapper.writeValueAsString(patch);
                version.setPatchData(patchStr);
                log.debug("保存增量版本 {}，patch 大小: {} 字节", 
                         note.getVersion(), patchStr.length());
            } catch (Exception e) {
                // 如果计算 patch 失败，降级为快照
                log.warn("计算 patch 失败，降级为快照: {}", e.getMessage());
                version.setIsSnapshot(true);
                String content = note.getContent();
                version.setSnapshotData(isValidJson(content) ? content : "{}");
            }
        }
        
        version.setChangeSummary("版本 " + note.getVersion());
        noteVersionRepository.save(version);
    }
    
    /**
     * 恢复指定版本的内容
     * 
     * 恢复流程：
     * 1. 找到目标版本之前最近的快照
     * 2. 获取快照内容
     * 3. 按顺序应用从快照到目标版本之间的所有 patch
     * 
     * 示例：恢复版本7
     *   版本1 [快照] → 获取内容 A
     *   版本2 [patch] → A + patch2 = B
     *   版本3 [patch] → B + patch3 = C
     *   ...
     *   版本7 [patch] → F + patch7 = G ✅
     */
    private String restoreContentToVersion(String noteUid, Integer targetVersion) {
        // 1. 先检查目标版本是否存在
        NoteVersion targetVersionEntity = noteVersionRepository.findByNoteUidAndVersion(noteUid, targetVersion)
                .orElse(null);
        
        if (targetVersionEntity == null) {
            log.warn("版本 {} 不存在", targetVersion);
            return "";
        }
        
        // 2. 如果目标版本本身是快照，直接返回
        if (Boolean.TRUE.equals(targetVersionEntity.getIsSnapshot()) 
                && targetVersionEntity.getSnapshotData() != null) {
            log.debug("版本 {} 是快照，直接返回", targetVersion);
            return targetVersionEntity.getSnapshotData();
        }
        
        // 3. 找到目标版本之前最近的快照
        NoteVersion snapshot = noteVersionRepository.findLatestSnapshotBeforeVersion(noteUid, targetVersion)
                .orElse(null);
        
        if (snapshot == null) {
            log.error("未找到版本 {} 之前的快照", targetVersion);
            return "";
        }
        
        log.debug("从快照版本 {} 开始恢复到版本 {}", snapshot.getVersion(), targetVersion);
        
        // 4. 获取快照内容
        String snapshotData = snapshot.getSnapshotData();
        if (snapshotData == null || snapshotData.trim().isEmpty()) {
            log.warn("快照版本 {} 内容为空", snapshot.getVersion());
            return "";
        }
        
        // 5. 如果快照版本就是目标版本，直接返回
        if (snapshot.getVersion().equals(targetVersion)) {
            return snapshotData;
        }
        
        // 6. 获取从快照到目标版本之间的所有版本（按版本号升序）
        List<NoteVersion> patchVersions = noteVersionRepository.findVersionsBetween(
                noteUid, snapshot.getVersion(), targetVersion);
        
        if (patchVersions.isEmpty()) {
            log.debug("快照版本 {} 到目标版本 {} 之间没有 patch", snapshot.getVersion(), targetVersion);
            return snapshotData;
        }
        
        // 7. 依次应用 patch
        try {
            JsonNode content = objectMapper.readTree(snapshotData);
            
            for (NoteVersion patchVersion : patchVersions) {
                if (Boolean.TRUE.equals(patchVersion.getIsSnapshot())) {
                    // 如果遇到快照，直接使用快照内容
                    if (patchVersion.getSnapshotData() != null) {
                        content = objectMapper.readTree(patchVersion.getSnapshotData());
                        log.debug("应用快照版本 {}", patchVersion.getVersion());
                    }
                } else if (patchVersion.getPatchData() != null && !patchVersion.getPatchData().isEmpty()) {
                    // 应用 patch
                    JsonNode patch = objectMapper.readTree(patchVersion.getPatchData());
                    content = JsonPatch.apply(patch, content);
                    log.debug("应用 patch 版本 {}", patchVersion.getVersion());
                }
            }
            
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            log.error("恢复版本 {} 失败: {}", targetVersion, e.getMessage(), e);
            return "";
        }
    }
    
    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setUid(note.getUid());
        dto.setFolderUid(note.getFolderUid());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setContentText(note.getContentText());
        dto.setWordCount(note.getWordCount());
        dto.setVersion(note.getVersion());
        dto.setIsPinned(note.getIsPinned());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setTags(getNoteTags(note.getUid()));
        return dto;
    }
    
    private NoteListDTO convertToListDTO(Note note) {
        NoteListDTO dto = new NoteListDTO();
        dto.setUid(note.getUid());
        dto.setFolderUid(note.getFolderUid());
        dto.setTitle(note.getTitle());
        dto.setWordCount(note.getWordCount());
        dto.setIsPinned(note.getIsPinned());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setTags(getNoteTags(note.getUid()));
        
        // 生成摘要
        String text = note.getContentText();
        if (text != null && text.length() > 100) {
            dto.setSummary(text.substring(0, 100) + "...");
        } else {
            dto.setSummary(text);
        }
        
        return dto;
    }
    
    private List<TagDTO> getNoteTags(String noteUid) {
        List<String> tagUids = noteTagRepository.findTagUidsByNoteUid(noteUid);
        if (tagUids.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Tag> tags = tagRepository.findByUidInAndDeletedFalse(tagUids);
        return tags.stream().map(tag -> {
            TagDTO dto = new TagDTO();
            dto.setUid(tag.getUid());
            dto.setName(tag.getName());
            dto.setColor(tag.getColor());
            return dto;
        }).collect(Collectors.toList());
    }
    
    private NoteVersionDTO convertVersionToDTO(NoteVersion version) {
        NoteVersionDTO dto = new NoteVersionDTO();
        dto.setUid(version.getUid());
        dto.setVersion(version.getVersion());
        dto.setIsSnapshot(version.getIsSnapshot());
        dto.setChangeSummary(version.getChangeSummary());
        dto.setCreatedAt(version.getCreatedAt());
        return dto;
    }
}

