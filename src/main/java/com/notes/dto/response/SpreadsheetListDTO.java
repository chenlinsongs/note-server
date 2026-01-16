package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpreadsheetListDTO {
    
    private String uid;
    private String folderUid;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
