package com.notes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TagDTO {
    
    private String uid;
    private String name;
    private String color;
    private Integer usageCount;
    private LocalDateTime createdAt;
}



