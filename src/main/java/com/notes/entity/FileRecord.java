package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "file_records")
@Getter
@Setter
public class FileRecord extends BaseEntity {
    
    @Column(name = "note_uid", length = 64)
    private String noteUid;
    
    @Column(name = "original_name", length = 255)
    private String originalName;
    
    @Column(name = "stored_name", length = 255)
    private String storedName;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "file_hash", length = 64)
    private String fileHash;
}
