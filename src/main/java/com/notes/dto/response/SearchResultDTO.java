package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SearchResultDTO {
    
    private String uid;
    private String title;
    private String highlightTitle;
    private String highlightContent;
    private String folderUid;
    private List<String> tags;
    private LocalDateTime updatedAt;
    private Float score;
}

