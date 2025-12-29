package com.notes.service;

import com.notes.dto.request.TagRequest;
import com.notes.dto.response.TagDTO;

import java.util.List;

public interface TagService {
    
    List<TagDTO> getAllTags();
    
    TagDTO createTag(TagRequest request);
    
    TagDTO updateTag(String uid, TagRequest request);
    
    void deleteTag(String uid);
}



