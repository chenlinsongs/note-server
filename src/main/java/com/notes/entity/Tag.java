package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tags")
@Getter
@Setter
public class Tag extends BaseEntity {
    
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(name = "color", length = 20)
    private String color = "#1890ff";
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
}

