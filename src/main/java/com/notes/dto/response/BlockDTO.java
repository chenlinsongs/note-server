package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockDTO {
    
    private String uid;
    private String noteUid;
    private String blockType;
    private String data;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



