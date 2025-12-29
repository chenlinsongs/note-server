package com.notes.service.impl;

import com.notes.dto.request.BlockRequest;
import com.notes.dto.response.BlockDTO;
import com.notes.entity.EmbedBlock;
import com.notes.exception.ResourceNotFoundException;
import com.notes.repository.EmbedBlockRepository;
import com.notes.service.BlockService;
import com.notes.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {
    
    private final EmbedBlockRepository embedBlockRepository;
    
    @Override
    public BlockDTO getBlock(String uid) {
        EmbedBlock block = embedBlockRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("嵌入块", "uid", uid));
        return convertToDTO(block);
    }
    
    @Override
    @Transactional
    public BlockDTO createBlock(BlockRequest request) {
        EmbedBlock block = new EmbedBlock();
        block.setUid(UidGenerator.generateBlockUid());
        block.setNoteUid(request.getNoteUid());
        block.setBlockType(request.getBlockType());
        block.setData(request.getData());
        
        block = embedBlockRepository.save(block);
        return convertToDTO(block);
    }
    
    @Override
    @Transactional
    public BlockDTO updateBlock(String uid, String data) {
        EmbedBlock block = embedBlockRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("嵌入块", "uid", uid));
        
        block.setData(data);
        block.setVersion(block.getVersion() + 1);
        
        block = embedBlockRepository.save(block);
        return convertToDTO(block);
    }
    
    @Override
    @Transactional
    public void deleteBlock(String uid) {
        EmbedBlock block = embedBlockRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("嵌入块", "uid", uid));
        
        block.setDeleted(true);
        block.setDeletedAt(LocalDateTime.now());
        embedBlockRepository.save(block);
    }
    
    private BlockDTO convertToDTO(EmbedBlock block) {
        BlockDTO dto = new BlockDTO();
        dto.setUid(block.getUid());
        dto.setNoteUid(block.getNoteUid());
        dto.setBlockType(block.getBlockType());
        dto.setData(block.getData());
        dto.setVersion(block.getVersion());
        dto.setCreatedAt(block.getCreatedAt());
        dto.setUpdatedAt(block.getUpdatedAt());
        return dto;
    }
}



