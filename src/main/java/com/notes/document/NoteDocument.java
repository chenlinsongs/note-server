package com.notes.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(indexName = "notes")
@Setting(shards = 1, replicas = 0)
public class NoteDocument {
    
    @Id
    @Field(name = "note_uid", type = FieldType.Keyword)
    private String noteUid;
    
    @Field(name = "folder_uid", type = FieldType.Keyword)
    private String folderUid;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    @Field(name = "content_text", type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    @Field(name = "word_count", type = FieldType.Integer)
    private Integer wordCount;
    
    @Field(name = "created_at", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime createdAt;
    
    @Field(name = "updated_at", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime updatedAt;
}

