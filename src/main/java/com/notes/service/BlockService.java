package com.notes.service;

import com.notes.dto.request.BlockRequest;
import com.notes.dto.response.BlockDTO;

public interface BlockService {
    
    BlockDTO getBlock(String uid);
    
    BlockDTO createBlock(BlockRequest request);
    
    BlockDTO updateBlock(String uid, String data);
    
    void deleteBlock(String uid);
}

