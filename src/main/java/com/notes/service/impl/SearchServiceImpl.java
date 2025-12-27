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
        try {
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
            
        } catch (Exception e) {
            log.error("搜索失败", e);
            return Page.empty(pageable);
        }
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

