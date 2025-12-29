package com.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class NoteRequest {
    
    @NotBlank(message = "笔记标题不能为空")
    private String title;
    
    private String content;  // TipTap JSON
    
    private String folderUid;
    
    private List<String> tagUids;
    
    private Boolean isPinned;
}



