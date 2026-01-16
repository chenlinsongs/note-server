package com.notes.service.impl;

import com.notes.dto.request.SpreadsheetRequest;
import com.notes.dto.response.SpreadsheetDTO;
import com.notes.dto.response.SpreadsheetListDTO;
import com.notes.entity.Spreadsheet;
import com.notes.exception.ResourceNotFoundException;
import com.notes.repository.SpreadsheetRepository;
import com.notes.service.SpreadsheetService;
import com.notes.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpreadsheetServiceImpl implements SpreadsheetService {
    
    private final SpreadsheetRepository spreadsheetRepository;
    
    private static final String DEFAULT_TITLE = "未命名表格";
    private static final String DEFAULT_DATA = "{}";
    
    @Override
    public Page<SpreadsheetListDTO> getSpreadsheets(String folderUid, Pageable pageable) {
        Page<Spreadsheet> spreadsheets;
        if (folderUid != null && !folderUid.isEmpty()) {
            spreadsheets = spreadsheetRepository.findByFolderUidAndDeletedFalseOrderByUpdatedAtDesc(folderUid, pageable);
        } else {
            spreadsheets = spreadsheetRepository.findByDeletedFalseOrderByUpdatedAtDesc(pageable);
        }
        return spreadsheets.map(this::convertToListDTO);
    }
    
    @Override
    public List<SpreadsheetListDTO> getSpreadsheetsByFolder(String folderUid) {
        return spreadsheetRepository.findByFolderUidAndDeletedFalse(folderUid)
                .stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public SpreadsheetDTO getSpreadsheet(String uid) {
        Spreadsheet spreadsheet = spreadsheetRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("表格", "uid", uid));
        return convertToDTO(spreadsheet);
    }
    
    @Override
    @Transactional
    public SpreadsheetDTO createSpreadsheet(SpreadsheetRequest request) {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setUid(UidGenerator.generateSpreadsheetUid());
        
        String title = request.getTitle();
        spreadsheet.setTitle(title != null && !title.trim().isEmpty() ? title : DEFAULT_TITLE);
        spreadsheet.setData(request.getData() != null ? request.getData() : DEFAULT_DATA);
        spreadsheet.setFolderUid(request.getFolderUid());
        
        spreadsheet = spreadsheetRepository.save(spreadsheet);
        return convertToDTO(spreadsheet);
    }
    
    @Override
    @Transactional
    public SpreadsheetDTO updateSpreadsheet(String uid, SpreadsheetRequest request) {
        Spreadsheet spreadsheet = spreadsheetRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("表格", "uid", uid));
        
        String title = request.getTitle();
        spreadsheet.setTitle(title != null && !title.trim().isEmpty() ? title : DEFAULT_TITLE);
        
        if (request.getData() != null) {
            spreadsheet.setData(request.getData());
            spreadsheet.setVersion(spreadsheet.getVersion() + 1);
        }
        
        if (request.getFolderUid() != null) {
            spreadsheet.setFolderUid(request.getFolderUid());
        }
        
        spreadsheet = spreadsheetRepository.save(spreadsheet);
        return convertToDTO(spreadsheet);
    }
    
    @Override
    @Transactional
    public void deleteSpreadsheet(String uid) {
        Spreadsheet spreadsheet = spreadsheetRepository.findByUidAndDeletedFalse(uid)
                .orElseThrow(() -> new ResourceNotFoundException("表格", "uid", uid));
        
        spreadsheet.setDeleted(true);
        spreadsheet.setDeletedAt(LocalDateTime.now());
        spreadsheetRepository.save(spreadsheet);
    }
    
    // ================= 垃圾桶相关 =================
    
    @Override
    public Page<SpreadsheetListDTO> getDeletedSpreadsheets(Pageable pageable) {
        return spreadsheetRepository.findByDeletedTrueOrderByDeletedAtDesc(pageable)
                .map(this::convertToListDTO);
    }
    
    @Override
    @Transactional
    public void restoreSpreadsheet(String uid) {
        Spreadsheet spreadsheet = spreadsheetRepository.findByUidAndDeletedTrue(uid)
                .orElseThrow(() -> new ResourceNotFoundException("表格", "uid", uid));
        
        spreadsheet.setDeleted(false);
        spreadsheet.setDeletedAt(null);
        spreadsheetRepository.save(spreadsheet);
    }
    
    @Override
    @Transactional
    public void permanentlyDeleteSpreadsheet(String uid) {
        Spreadsheet spreadsheet = spreadsheetRepository.findByUidAndDeletedTrue(uid)
                .orElseThrow(() -> new ResourceNotFoundException("表格", "uid", uid));
        
        spreadsheetRepository.delete(spreadsheet);
    }
    
    @Override
    public long getDeletedCount() {
        return spreadsheetRepository.countDeleted();
    }
    
    // ================= 转换方法 =================
    
    private SpreadsheetDTO convertToDTO(Spreadsheet spreadsheet) {
        SpreadsheetDTO dto = new SpreadsheetDTO();
        dto.setUid(spreadsheet.getUid());
        dto.setFolderUid(spreadsheet.getFolderUid());
        dto.setTitle(spreadsheet.getTitle());
        dto.setData(spreadsheet.getData());
        dto.setVersion(spreadsheet.getVersion());
        dto.setCreatedAt(spreadsheet.getCreatedAt());
        dto.setUpdatedAt(spreadsheet.getUpdatedAt());
        return dto;
    }
    
    private SpreadsheetListDTO convertToListDTO(Spreadsheet spreadsheet) {
        SpreadsheetListDTO dto = new SpreadsheetListDTO();
        dto.setUid(spreadsheet.getUid());
        dto.setFolderUid(spreadsheet.getFolderUid());
        dto.setTitle(spreadsheet.getTitle());
        dto.setCreatedAt(spreadsheet.getCreatedAt());
        dto.setUpdatedAt(spreadsheet.getUpdatedAt());
        dto.setDeletedAt(spreadsheet.getDeletedAt());
        return dto;
    }
}
