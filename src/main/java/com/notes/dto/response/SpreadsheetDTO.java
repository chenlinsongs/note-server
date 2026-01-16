package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpreadsheetDTO {
    
    private String uid;
    private String folderUid;
    private String title;
    private String data;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
