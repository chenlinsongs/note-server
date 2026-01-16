package com.notes.service;

import com.notes.dto.request.SpreadsheetRequest;
import com.notes.dto.response.SpreadsheetDTO;
import com.notes.dto.response.SpreadsheetListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SpreadsheetService {
    
    Page<SpreadsheetListDTO> getSpreadsheets(String folderUid, Pageable pageable);
    
    List<SpreadsheetListDTO> getSpreadsheetsByFolder(String folderUid);
    
    SpreadsheetDTO getSpreadsheet(String uid);
    
    SpreadsheetDTO createSpreadsheet(SpreadsheetRequest request);
    
    SpreadsheetDTO updateSpreadsheet(String uid, SpreadsheetRequest request);
    
    void deleteSpreadsheet(String uid);
    
    // 垃圾桶
    Page<SpreadsheetListDTO> getDeletedSpreadsheets(Pageable pageable);
    
    void restoreSpreadsheet(String uid);
    
    void permanentlyDeleteSpreadsheet(String uid);
    
    long getDeletedCount();
}
