package com.notes.controller;

import com.notes.dto.request.SpreadsheetRequest;
import com.notes.dto.response.ApiResponse;
import com.notes.dto.response.SpreadsheetDTO;
import com.notes.dto.response.SpreadsheetListDTO;
import com.notes.service.SpreadsheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spreadsheets")
@RequiredArgsConstructor
public class SpreadsheetController {
    
    private final SpreadsheetService spreadsheetService;
    
    @GetMapping
    public ApiResponse<Page<SpreadsheetListDTO>> getSpreadsheets(
            @RequestParam(required = false) String folderUid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(spreadsheetService.getSpreadsheets(folderUid, pageable));
    }
    
    @GetMapping("/folder/{folderUid}")
    public ApiResponse<List<SpreadsheetListDTO>> getSpreadsheetsByFolder(@PathVariable String folderUid) {
        return ApiResponse.success(spreadsheetService.getSpreadsheetsByFolder(folderUid));
    }
    
    @GetMapping("/{uid}")
    public ApiResponse<SpreadsheetDTO> getSpreadsheet(@PathVariable String uid) {
        return ApiResponse.success(spreadsheetService.getSpreadsheet(uid));
    }
    
    @PostMapping
    public ApiResponse<SpreadsheetDTO> createSpreadsheet(@Valid @RequestBody SpreadsheetRequest request) {
        return ApiResponse.success("表格创建成功", spreadsheetService.createSpreadsheet(request));
    }
    
    @PutMapping("/{uid}")
    public ApiResponse<SpreadsheetDTO> updateSpreadsheet(@PathVariable String uid, @Valid @RequestBody SpreadsheetRequest request) {
        return ApiResponse.success("表格更新成功", spreadsheetService.updateSpreadsheet(uid, request));
    }
    
    @DeleteMapping("/{uid}")
    public ApiResponse<Void> deleteSpreadsheet(@PathVariable String uid) {
        spreadsheetService.deleteSpreadsheet(uid);
        return ApiResponse.success("表格删除成功", null);
    }
    
    // ================= 垃圾桶相关 =================
    
    @GetMapping("/trash/list")
    public ApiResponse<Page<SpreadsheetListDTO>> getDeletedSpreadsheets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(spreadsheetService.getDeletedSpreadsheets(pageable));
    }
    
    @PostMapping("/trash/{uid}/restore")
    public ApiResponse<Void> restoreSpreadsheet(@PathVariable String uid) {
        spreadsheetService.restoreSpreadsheet(uid);
        return ApiResponse.success("表格已恢复", null);
    }
    
    @DeleteMapping("/trash/{uid}")
    public ApiResponse<Void> permanentlyDeleteSpreadsheet(@PathVariable String uid) {
        spreadsheetService.permanentlyDeleteSpreadsheet(uid);
        return ApiResponse.success("表格已永久删除", null);
    }
    
    @GetMapping("/trash/count")
    public ApiResponse<Long> getDeletedCount() {
        return ApiResponse.success(spreadsheetService.getDeletedCount());
    }
}
