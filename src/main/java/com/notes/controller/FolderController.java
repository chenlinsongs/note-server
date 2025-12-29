package com.notes.controller;

import com.notes.dto.request.FolderRequest;
import com.notes.dto.response.ApiResponse;
import com.notes.dto.response.FolderDTO;
import com.notes.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {
    
    private final FolderService folderService;
    
    @GetMapping
    public ApiResponse<List<FolderDTO>> getFolderTree() {
        return ApiResponse.success(folderService.getFolderTree());
    }
    
    @GetMapping("/{uid}")
    public ApiResponse<FolderDTO> getFolder(@PathVariable String uid) {
        return ApiResponse.success(folderService.getFolder(uid));
    }
    
    @PostMapping
    public ApiResponse<FolderDTO> createFolder(@Valid @RequestBody FolderRequest request) {
        return ApiResponse.success("文件夹创建成功", folderService.createFolder(request));
    }
    
    @PutMapping("/{uid}")
    public ApiResponse<FolderDTO> updateFolder(@PathVariable String uid, @Valid @RequestBody FolderRequest request) {
        return ApiResponse.success("文件夹更新成功", folderService.updateFolder(uid, request));
    }
    
    @DeleteMapping("/{uid}")
    public ApiResponse<Void> deleteFolder(@PathVariable String uid) {
        folderService.deleteFolder(uid);
        return ApiResponse.success("文件夹删除成功", null);
    }
    
    @PutMapping("/{uid}/move")
    public ApiResponse<Void> moveFolder(@PathVariable String uid, @RequestBody Map<String, String> body) {
        String newParentUid = body.get("parentUid");
        folderService.moveFolder(uid, newParentUid);
        return ApiResponse.success("文件夹移动成功", null);
    }
}



