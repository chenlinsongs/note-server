package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "folders")
@Getter
@Setter
public class Folder extends BaseEntity {
    
    @Column(name = "parent_uid", length = 64)
    private String parentUid;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "path", length = 5000)
    private String path = "/";
    
    @Column(name = "level")
    private Integer level = 0;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}

