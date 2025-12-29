package com.notes.dto.response;

import lombok.Data;

@Data
public class FileUploadDTO {
    
    private String uid;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
}



