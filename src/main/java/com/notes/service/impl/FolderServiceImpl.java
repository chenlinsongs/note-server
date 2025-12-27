package com.notes.service.impl;

import com.notes.dto.request.FolderRequest;
import com.notes.dto.response.FolderDTO;
import com.notes.entity.Folder;
import com.notes.exception.BusinessException;
import com.notes.exception.ResourceNotFoundException;
import com.notes.repository.FolderRepository;
import com.notes.repository.NoteRepository;
import com.notes.service.FolderService;
import com.notes.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {
    
    private final FolderRepository folderRepository;
    private final NoteRepository noteRepository;
    
    @Override
    public List<FolderDTO> getFolderTree() {
        List<Folder> folders = folderRepository.findByDeletedFalseOrderByPathAsc();
        return buildTree(folders);
    }
    
    @Override
    public FolderDTO getFolder(String uid) {
        Folder folder = folderRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("文件夹", "uid", uid));
        return convertToDTO(folder);
    }
    
    @Override
    @Transactional
    public FolderDTO createFolder(FolderRequest request) {
        Folder folder = new Folder();
        folder.setUid(UidGenerator.generateFolderUid());
        folder.setName(request.getName());
        folder.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        
        if (request.getParentUid() != null && !request.getParentUid().isEmpty()) {
            Folder parent = folderRepository.findByUidAndDeletedFalse(request.getParentUid())
                    .orElseThrow(() -> new ResourceNotFoundException("父文件夹", "uid", request.getParentUid()));
            folder.setParentUid(request.getParentUid());
            folder.setPath(parent.getPath() + "/" + folder.getUid());
            folder.setLevel(parent.getLevel() + 1);
        } else {
            folder.setPath("/" + folder.getUid());
            folder.setLevel(0);
        }
        
        folder = folderRepository.save(folder);
        return convertToDTO(folder);
    }
    
    @Override
    @Transactional
    public FolderDTO updateFolder(String uid, FolderRequest request) {
        Folder folder = folderRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("文件夹", "uid", uid));
        
        folder.setName(request.getName());
        if (request.getSortOrder() != null) {
            folder.setSortOrder(request.getSortOrder());
        }
        
        folder = folderRepository.save(folder);
        return convertToDTO(folder);
    }
    
    @Override
    @Transactional
    public void deleteFolder(String uid) {
        Folder folder = folderRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("文件夹", "uid", uid));
        
        // 检查是否有笔记
        Integer noteCount = noteRepository.countByFolderUid(uid);
        if (noteCount > 0) {
            throw new BusinessException("文件夹下存在笔记，无法删除");
        }
        
        // 检查是否有子文件夹
        List<Folder> children = folderRepository.findByParentUidAndDeletedFalseOrderBySortOrderAsc(uid);
        if (!children.isEmpty()) {
            throw new BusinessException("文件夹下存在子文件夹，无法删除");
        }
        
        folder.setDeleted(true);
        folder.setDeletedAt(LocalDateTime.now());
        folderRepository.save(folder);
    }
    
    @Override
    @Transactional
    public void moveFolder(String uid, String newParentUid) {
        Folder folder = folderRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("文件夹", "uid", uid));
        
        String oldPath = folder.getPath();
        
        if (newParentUid != null && !newParentUid.isEmpty()) {
            Folder newParent = folderRepository.findByUidAndDeletedFalse(newParentUid)
                    .orElseThrow(() -> new ResourceNotFoundException("目标文件夹", "uid", newParentUid));
            
            // 检查是否移动到自己的子文件夹
            if (newParent.getPath().startsWith(oldPath)) {
                throw new BusinessException("不能将文件夹移动到自己的子文件夹中");
            }
            
            folder.setParentUid(newParentUid);
            folder.setPath(newParent.getPath() + "/" + folder.getUid());
            folder.setLevel(newParent.getLevel() + 1);
        } else {
            folder.setParentUid(null);
            folder.setPath("/" + folder.getUid());
            folder.setLevel(0);
        }
        
        folderRepository.save(folder);
        
        // 更新所有子文件夹的路径
        updateChildrenPaths(oldPath, folder.getPath());
    }
    
    private void updateChildrenPaths(String oldPath, String newPath) {
        List<Folder> children = folderRepository.findByPathStartingWith(oldPath + "/");
        for (Folder child : children) {
            child.setPath(child.getPath().replace(oldPath, newPath));
            child.setLevel(child.getPath().split("/").length - 1);
            folderRepository.save(child);
        }
    }
    
    private List<FolderDTO> buildTree(List<Folder> folders) {
        Map<String, FolderDTO> dtoMap = new HashMap<>();
        List<FolderDTO> roots = new ArrayList<>();
        
        // 转换为 DTO 并建立映射
        for (Folder folder : folders) {
            FolderDTO dto = convertToDTO(folder);
            dtoMap.put(folder.getUid(), dto);
        }
        
        // 构建树结构
        for (Folder folder : folders) {
            FolderDTO dto = dtoMap.get(folder.getUid());
            if (folder.getParentUid() == null || folder.getParentUid().isEmpty()) {
                roots.add(dto);
            } else {
                FolderDTO parent = dtoMap.get(folder.getParentUid());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        
        return roots;
    }
    
    private FolderDTO convertToDTO(Folder folder) {
        FolderDTO dto = new FolderDTO();
        dto.setUid(folder.getUid());
        dto.setParentUid(folder.getParentUid());
        dto.setName(folder.getName());
        dto.setPath(folder.getPath());
        dto.setLevel(folder.getLevel());
        dto.setSortOrder(folder.getSortOrder());
        dto.setCreatedAt(folder.getCreatedAt());
        dto.setUpdatedAt(folder.getUpdatedAt());
        dto.setNoteCount(noteRepository.countByFolderUid(folder.getUid()));
        return dto;
    }
}

