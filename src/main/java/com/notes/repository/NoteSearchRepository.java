package com.notes.repository;

import com.notes.document.NoteDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteSearchRepository extends ElasticsearchRepository<NoteDocument, String> {
}

