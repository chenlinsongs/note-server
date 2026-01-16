package com.notes.repository;

import com.notes.entity.Spreadsheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpreadsheetRepository extends JpaRepository<Spreadsheet, Long> {
    
    Optional<Spreadsheet> findByUidAndDeletedFalse(String uid);
    
    Page<Spreadsheet> findByDeletedFalseOrderByUpdatedAtDesc(Pageable pageable);
    
    Page<Spreadsheet> findByFolderUidAndDeletedFalseOrderByUpdatedAtDesc(String folderUid, Pageable pageable);
    
    List<Spreadsheet> findByFolderUidAndDeletedFalse(String folderUid);
    
    @Query("SELECT COUNT(s) FROM Spreadsheet s WHERE s.folderUid = :folderUid AND s.deleted = false")
    Integer countByFolderUid(String folderUid);
    
    boolean existsByUidAndDeletedFalse(String uid);
    
    // 垃圾桶
    Page<Spreadsheet> findByDeletedTrueOrderByDeletedAtDesc(Pageable pageable);
    
    Optional<Spreadsheet> findByUidAndDeletedTrue(String uid);
    
    @Query("SELECT COUNT(s) FROM Spreadsheet s WHERE s.deleted = true")
    long countDeleted();
}
