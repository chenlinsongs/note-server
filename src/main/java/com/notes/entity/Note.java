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
    
    // 笔记类型: document(普通文档), spreadsheet(表格), mindmap(思维导图), canvas(画布)
    @Column(name = "note_type", length = 20)
    private String noteType = "document";
    
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
    
    // 排序顺序，用于保持文档添加顺序，值越小越靠前
    @Column(name = "sort_order")
    private Long sortOrder;
}



