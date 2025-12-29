package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class FolderDTO {
    
    private String uid;
    private String parentUid;
    private String name;
    private String path;
    private Integer level;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FolderDTO> children = new ArrayList<>();
    private Integer noteCount;
}



