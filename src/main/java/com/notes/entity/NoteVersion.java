package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "note_versions")
@Getter
@Setter
public class NoteVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "uid", nullable = false, unique = true, length = 64)
    private String uid;
    
    @Column(name = "note_uid", nullable = false, length = 64)
    private String noteUid;
    
    @Column(name = "version", nullable = false)
    private Integer version;
    
    @Column(name = "is_snapshot")
    private Boolean isSnapshot = false;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "patch_data", columnDefinition = "json")
    private String patchData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", columnDefinition = "json")
    private String snapshotData;
    
    @Column(name = "content_hash", length = 64)
    private String contentHash;
    
    @Column(name = "change_summary", length = 500)
    private String changeSummary;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

