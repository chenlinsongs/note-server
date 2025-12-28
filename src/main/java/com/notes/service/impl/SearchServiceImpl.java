package com.notes.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.notes.document.NoteDocument;
import com.notes.dto.response.SearchResultDTO;
import com.notes.entity.Note;
import com.notes.repository.NoteRepository;
import com.notes.repository.NoteSearchRepository;
import com.notes.repository.NoteTagRepository;
import com.notes.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    
    private final NoteSearchRepository noteSearchRepository;
    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;
    private final ElasticsearchClient elasticsearchClient;
    
    @Override
    public Page<SearchResultDTO> search(String keyword, String folderUid, Pageable pageable) {
        // 首先尝试使用 Elasticsearch 搜索
        try {
            return searchWithElasticsearch(keyword, folderUid, pageable);
        } catch (Exception e) {
            log.warn("Elasticsearch 搜索失败，降级到数据库搜索: {}", e.getMessage());
            // 降级到数据库搜索
            return searchWithDatabase(keyword, folderUid, pageable);
        }
    }
    
    /**
     * 使用 Elasticsearch 搜索
     */
    private Page<SearchResultDTO> searchWithElasticsearch(String keyword, String folderUid, Pageable pageable) throws Exception {
        // 构建查询
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        
        // 关键词匹配
        boolQueryBuilder.should(Query.of(q -> q
                .match(m -> m
                        .field("title")
                        .query(keyword)
                        .boost(2.0f))));
        
        boolQueryBuilder.should(Query.of(q -> q
                .match(m -> m
                        .field("content")
                        .query(keyword))));
        
        boolQueryBuilder.minimumShouldMatch("1");
        
        // 文件夹过滤
        if (folderUid != null && !folderUid.isEmpty()) {
            boolQueryBuilder.filter(Query.of(q -> q
                    .term(t -> t
                            .field("folderUid")
                            .value(folderUid))));
        }
        
        // 构建搜索请求
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("notes")
                .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize())
                .highlight(h -> h
                        .fields("title", HighlightField.of(hf -> hf
                                .preTags("<em>")
                                .postTags("</em>")))
                        .fields("content", HighlightField.of(hf -> hf
                                .preTags("<em>")
                                .postTags("</em>")
                                .fragmentSize(150)
                                .numberOfFragments(3)))));
        
        // 执行搜索
        SearchResponse<NoteDocument> response = elasticsearchClient.search(searchRequest, NoteDocument.class);
        
        // 转换结果
        List<SearchResultDTO> results = new ArrayList<>();
        for (Hit<NoteDocument> hit : response.hits().hits()) {
            NoteDocument doc = hit.source();
            if (doc != null) {
                SearchResultDTO dto = new SearchResultDTO();
                dto.setUid(doc.getNoteUid());
                dto.setTitle(doc.getTitle());
                dto.setFolderUid(doc.getFolderUid());
                dto.setTags(doc.getTags());
                dto.setUpdatedAt(doc.getUpdatedAt());
                dto.setScore(hit.score() != null ? hit.score().floatValue() : 0f);
                
                // 高亮结果
                Map<String, List<String>> highlights = hit.highlight();
                if (highlights != null) {
                    if (highlights.containsKey("title")) {
                        dto.setHighlightTitle(String.join("", highlights.get("title")));
                    }
                    if (highlights.containsKey("content")) {
                        dto.setHighlightContent(String.join("...", highlights.get("content")));
                    }
                }
                
                results.add(dto);
            }
        }
        
        long total = response.hits().total() != null ? response.hits().total().value() : 0;
        return new PageImpl<>(results, pageable, total);
    }
    
    /**
     * 使用数据库搜索（Elasticsearch 不可用时的降级方案）
     */
    private Page<SearchResultDTO> searchWithDatabase(String keyword, String folderUid, Pageable pageable) {
        Page<Note> notes;
        if (folderUid != null && !folderUid.isEmpty()) {
            notes = noteRepository.searchByKeywordAndFolder(keyword, folderUid, pageable);
        } else {
            notes = noteRepository.searchByKeyword(keyword, pageable);
        }
        
        List<SearchResultDTO> results = notes.getContent().stream().map(note -> {
            SearchResultDTO dto = new SearchResultDTO();
            dto.setUid(note.getUid());
            dto.setTitle(note.getTitle());
            dto.setFolderUid(note.getFolderUid());
            dto.setUpdatedAt(note.getUpdatedAt());
            dto.setScore(1.0f);
            
            // 简单的高亮处理
            String title = note.getTitle();
            String contentText = note.getContentText();
            
            if (title != null && title.toLowerCase().contains(keyword.toLowerCase())) {
                dto.setHighlightTitle(title.replaceAll("(?i)(" + escapeRegex(keyword) + ")", "<em>$1</em>"));
            }
            
            if (contentText != null && contentText.toLowerCase().contains(keyword.toLowerCase())) {
                // 截取关键词附近的内容片段
                int idx = contentText.toLowerCase().indexOf(keyword.toLowerCase());
                int start = Math.max(0, idx - 50);
                int end = Math.min(contentText.length(), idx + keyword.length() + 100);
                String fragment = (start > 0 ? "..." : "") + 
                                  contentText.substring(start, end) + 
                                  (end < contentText.length() ? "..." : "");
                dto.setHighlightContent(fragment.replaceAll("(?i)(" + escapeRegex(keyword) + ")", "<em>$1</em>"));
            }
            
            return dto;
        }).toList();
        
        return new PageImpl<>(results, pageable, notes.getTotalElements());
    }
    
    /**
     * 转义正则表达式特殊字符
     */
    private String escapeRegex(String str) {
        return str.replaceAll("([\\\\\\[\\](){}.*+?^$|])", "\\\\$1");
    }
    
    @Override
    public void indexNote(String noteUid) {
        Note note = noteRepository.findByUidAndDeletedFalse(noteUid).orElse(null);
        if (note == null) {
            return;
        }
        
        NoteDocument doc = new NoteDocument();
        doc.setNoteUid(note.getUid());
        doc.setFolderUid(note.getFolderUid());
        doc.setTitle(note.getTitle());
        doc.setContent(note.getContentText());
        doc.setWordCount(note.getWordCount());
        doc.setCreatedAt(note.getCreatedAt());
        doc.setUpdatedAt(note.getUpdatedAt());
        
        // 获取标签
        List<String> tagUids = noteTagRepository.findTagUidsByNoteUid(noteUid);
        doc.setTags(tagUids);
        
        noteSearchRepository.save(doc);
    }
    
    @Override
    public void deleteNoteIndex(String noteUid) {
        noteSearchRepository.deleteById(noteUid);
    }
    
    @Override
    public void reindexAll() {
        List<Note> notes = noteRepository.findByDeletedFalse();
        for (Note note : notes) {
            try {
                indexNote(note.getUid());
            } catch (Exception e) {
                log.error("索引笔记失败: {}", note.getUid(), e);
            }
        }
    }
}

