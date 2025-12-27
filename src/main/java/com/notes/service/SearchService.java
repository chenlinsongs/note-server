package com.notes.service;

import com.notes.dto.response.SearchResultDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    
    Page<SearchResultDTO> search(String keyword, String folderUid, Pageable pageable);
    
    void indexNote(String noteUid);
    
    void deleteNoteIndex(String noteUid);
    
    void reindexAll();
}

