package com.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class NoteRequest {
    
    @NotBlank(message = "笔记标题不能为空")
    private String title;
    
    private String content;  // TipTap JSON 或 Univer JSON
    
    private String folderUid;
    
    private List<String> tagUids;
    
    private Boolean isPinned;
    
    // 笔记类型: document(普通文档), spreadsheet(表格)
    private String noteType = "document";
}



