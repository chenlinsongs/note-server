package com.notes.controller;

import com.notes.dto.response.ApiResponse;
import com.notes.dto.response.FileUploadDTO;
import com.notes.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    public ApiResponse<FileUploadDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "noteUid", required = false) String noteUid) {
        return ApiResponse.success("文件上传成功", fileService.uploadFile(file, noteUid));
    }
}

