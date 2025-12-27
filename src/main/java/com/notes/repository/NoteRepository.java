package com.notes.repository;

import com.notes.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    Optional<Note> findByUidAndDeletedFalse(String uid);
    
    Page<Note> findByDeletedFalseOrderByIsPinnedDescUpdatedAtDesc(Pageable pageable);
    
    Page<Note> findByFolderUidAndDeletedFalseOrderByIsPinnedDescUpdatedAtDesc(String folderUid, Pageable pageable);
    
    List<Note> findByDeletedFalse();
    
    @Query("SELECT COUNT(n) FROM Note n WHERE n.folderUid = :folderUid AND n.deleted = false")
    Integer countByFolderUid(String folderUid);
    
    boolean existsByUidAndDeletedFalse(String uid);
}

