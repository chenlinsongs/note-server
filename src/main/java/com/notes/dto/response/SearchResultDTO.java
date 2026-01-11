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
    private String folderName;
    private String folderPath; // 完整文件夹路径，如 "父文件夹 / 子文件夹"
    private List<String> tags;
    private LocalDateTime updatedAt;
    private Float score;
}



