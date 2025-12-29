package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteVersionDTO {
    
    private String uid;
    private Integer version;
    private Boolean isSnapshot;
    private String changeSummary;
    private LocalDateTime createdAt;
}



