package com.notes.service;

import com.notes.dto.response.FileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    
    FileUploadDTO uploadFile(MultipartFile file, String noteUid);
}



