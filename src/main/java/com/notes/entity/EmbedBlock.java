package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "embed_blocks")
@Getter
@Setter
public class EmbedBlock extends BaseEntity {
    
    @Column(name = "note_uid", nullable = false, length = 64)
    private String noteUid;
    
    @Column(name = "block_type", nullable = false, length = 50)
    private String blockType;
    
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;
    
    @Column(name = "version")
    private Integer version = 1;
}

