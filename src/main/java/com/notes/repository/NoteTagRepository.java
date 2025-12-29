package com.notes.repository;

import com.notes.entity.NoteTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteTagRepository extends JpaRepository<NoteTag, Long> {
    
    List<NoteTag> findByNoteUid(String noteUid);
    
    List<NoteTag> findByTagUid(String tagUid);
    
    void deleteByNoteUid(String noteUid);
    
    void deleteByNoteUidAndTagUid(String noteUid, String tagUid);
    
    @Query("SELECT nt.tagUid FROM NoteTag nt WHERE nt.noteUid = :noteUid")
    List<String> findTagUidsByNoteUid(String noteUid);
    
    boolean existsByNoteUidAndTagUid(String noteUid, String tagUid);
}



