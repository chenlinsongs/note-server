package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notes")
@Getter
@Setter
public class Note extends BaseEntity {
    
    @Column(name = "folder_uid", length = 64)
    private String folderUid;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "json")
    private String content;
    
    @Column(name = "content_text", columnDefinition = "MEDIUMTEXT")
    private String contentText;
    
    @Column(name = "content_markdown", columnDefinition = "MEDIUMTEXT")
    private String contentMarkdown;
    
    @Column(name = "word_count")
    private Integer wordCount = 0;
    
    @Column(name = "version")
    private Integer version = 1;
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false;
}

