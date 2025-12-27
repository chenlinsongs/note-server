package com.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlockRequest {
    
    @NotBlank(message = "块类型不能为空")
    private String blockType;
    
    @NotBlank(message = "笔记UID不能为空")
    private String noteUid;
    
    private String data;
}

