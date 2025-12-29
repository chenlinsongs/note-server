package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "embed_block_versions")
@Getter
@Setter
public class EmbedBlockVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "uid", nullable = false, unique = true, length = 64)
    private String uid;
    
    @Column(name = "block_uid", nullable = false, length = 64)
    private String blockUid;
    
    @Column(name = "version", nullable = false)
    private Integer version;
    
    @Column(name = "is_snapshot")
    private Boolean isSnapshot = false;
    
    @Column(name = "patch_data", columnDefinition = "TEXT")
    private String patchData;
    
    @Column(name = "snapshot_data", columnDefinition = "TEXT")
    private String snapshotData;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}



