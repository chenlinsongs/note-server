package com.notes.service.impl;

import com.notes.dto.request.TagRequest;
import com.notes.dto.response.TagDTO;
import com.notes.entity.Tag;
import com.notes.exception.BusinessException;
import com.notes.exception.ResourceNotFoundException;
import com.notes.repository.NoteTagRepository;
import com.notes.repository.TagRepository;
import com.notes.service.TagService;
import com.notes.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    
    private final TagRepository tagRepository;
    private final NoteTagRepository noteTagRepository;
    
    @Override
    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagRepository.findByDeletedFalseOrderByUsageCountDesc();
        return tags.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public TagDTO createTag(TagRequest request) {
        if (tagRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("标签名称已存在");
        }
        
        Tag tag = new Tag();
        tag.setUid(UidGenerator.generateTagUid());
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        
        tag = tagRepository.save(tag);
        return convertToDTO(tag);
    }
    
    @Override
    @Transactional
    public TagDTO updateTag(String uid, TagRequest request) {
        Tag tag = tagRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("标签", "uid", uid));
        
        // 检查名称是否重复
        tagRepository.findByNameAndDeletedFalse(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getUid().equals(uid)) {
                        throw new BusinessException("标签名称已存在");
                    }
                });
        
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        
        tag = tagRepository.save(tag);
        return convertToDTO(tag);
    }
    
    @Override
    @Transactional
    public void deleteTag(String uid) {
        Tag tag = tagRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("标签", "uid", uid));
        
        tag.setDeleted(true);
        tag.setDeletedAt(LocalDateTime.now());
        tagRepository.save(tag);
    }
    
    private TagDTO convertToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setUid(tag.getUid());
        dto.setName(tag.getName());
        dto.setColor(tag.getColor());
        dto.setUsageCount(tag.getUsageCount());
        dto.setCreatedAt(tag.getCreatedAt());
        return dto;
    }
}

