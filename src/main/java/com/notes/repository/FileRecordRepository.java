package com.notes.repository;

import com.notes.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    
    Optional<FileRecord> findByUidAndDeletedFalse(String uid);
    
    List<FileRecord> findByNoteUidAndDeletedFalse(String noteUid);
    
    Optional<FileRecord> findByFileHashAndDeletedFalse(String fileHash);
}



