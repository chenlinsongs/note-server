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
        
        // 标题为空时使用默认标题
        String title = request.getTitle();
        note.setTitle(title != null && !title.trim().isEmpty() ? title : DEFAULT_TITLE);
        note.setContent(request.getContent());
        if (request.getFolderUid() != null) {
            note.setFolderUid(request.getFolderUid());
        }
        if (request.getIsPinned() != null) {
            note.setIsPinned(request.getIsPinned());
        }
        
        // 提取纯文本和字数
        String contentText = contentExtractor.extractText(request.getContent());
        note.setContentText(contentText);
        note.setWordCount(contentExtractor.countWords(contentText));
        note.setVersion(note.getVersion() + 1);
        
        note = noteRepository.save(note);
        
        // 更新标签关联
        if (request.getTagUids() != null) {
            noteTagRepository.deleteByNoteUid(uid);
            saveNoteTags(uid, request.getTagUids());
        }
        
        // 保存版本（增量或快照）
        boolean isSnapshot = note.getVersion() % SNAPSHOT_INTERVAL == 0;
        saveVersionWithDiff(note, oldContent, isSnapshot);
        
        // 更新 ES 索引
        try {
            searchService.indexNote(note.getUid());
        } catch (Exception e) {
            log.warn("更新索引失败: {}", e.getMessage());
        }
        
        return convertToDTO(note);
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
            // 确保快照数据不为空，空内容保存为空字符串
            String content = note.getContent();
            version.setSnapshotData(content != null ? content : "");
        }
        
        version.setChangeSummary("版本 " + note.getVersion());
        noteVersionRepository.save(version);
    }
    
    private void saveVersionWithDiff(Note note, String oldContent, boolean isSnapshot) {
        NoteVersion version = new NoteVersion();
        version.setUid(UidGenerator.generateNoteVersionUid());
        version.setNoteUid(note.getUid());
        version.setVersion(note.getVersion());
        // 简化：每个版本都保存完整内容，确保可以正确恢复
        version.setIsSnapshot(true);
        version.setSnapshotData(note.getContent());
        
        // 同时保存差异（用于对比显示）
        if (!isSnapshot && oldContent != null) {
            try {
                JsonNode oldNode = objectMapper.readTree(oldContent);
                JsonNode newNode = objectMapper.readTree(note.getContent() != null ? note.getContent() : "{}");
                JsonNode patch = JsonDiff.asJson(oldNode, newNode);
                version.setPatchData(objectMapper.writeValueAsString(patch));
            } catch (Exception e) {
                log.debug("计算差异失败: {}", e.getMessage());
            }
        }
        
        version.setChangeSummary("版本 " + note.getVersion());
        noteVersionRepository.save(version);
    }
    
    private String restoreContentToVersion(String noteUid, Integer targetVersion) {
        // 直接获取目标版本
        NoteVersion targetVersionEntity = noteVersionRepository.findByNoteUidAndVersion(noteUid, targetVersion)
                .orElse(null);
        
        // 每个版本都保存了完整快照，直接返回
        if (targetVersionEntity != null && targetVersionEntity.getSnapshotData() != null) {
            return targetVersionEntity.getSnapshotData();
        }
        
        log.warn("未找到版本 {} 的数据", targetVersion);
        return "";
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

