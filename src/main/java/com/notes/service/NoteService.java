package com.notes.service;

import com.notes.dto.request.NoteRequest;
import com.notes.dto.response.NoteDTO;
import com.notes.dto.response.NoteListDTO;
import com.notes.dto.response.NoteVersionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {
    
    Page<NoteListDTO> getNotes(String folderUid, Pageable pageable);
    
    NoteDTO getNote(String uid);
    
    NoteDTO createNote(NoteRequest request);
    
    NoteDTO updateNote(String uid, NoteRequest request);
    
    void deleteNote(String uid);
    
    List<NoteVersionDTO> getVersions(String uid);
    
    NoteDTO getVersion(String uid, Integer version);
    
    NoteDTO restoreVersion(String uid, Integer version);
    
    // 垃圾桶相关
    Page<NoteListDTO> getDeletedNotes(Pageable pageable);
    
    void restoreNote(String uid);
    
    void permanentlyDeleteNote(String uid);
    
    long getDeletedCount();
    
    NoteDTO getDeletedNote(String uid);
}



