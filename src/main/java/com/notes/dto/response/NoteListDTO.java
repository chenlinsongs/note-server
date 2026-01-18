package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteListDTO {
    
    private String uid;
    private String folderUid;
    private String title;
    private String summary;  // 内容摘要
    private Integer wordCount;
    private Boolean isPinned;
    private String noteType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;  // 删除时间
    private List<TagDTO> tags;
}



