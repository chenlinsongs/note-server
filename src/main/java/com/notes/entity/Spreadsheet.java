package com.notes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "spreadsheets")
@Getter
@Setter
public class Spreadsheet extends BaseEntity {
    
    @Column(name = "folder_uid", length = 64)
    private String folderUid;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json")
    private String data;  // Univer 表格数据 JSON
    
    @Column(name = "version")
    private Integer version = 1;
}
