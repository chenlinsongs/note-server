package com.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    
    @NotBlank(message = "标签名称不能为空")
    private String name;
    
    private String color = "#1890ff";
}

