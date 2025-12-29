package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDTO {
    
    private String uid;
    private String folderUid;
    private String title;
    private String content;
    private String contentText;
    private Integer wordCount;
    private Integer version;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagDTO> tags;
}



