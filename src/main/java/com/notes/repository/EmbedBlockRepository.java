package com.notes.repository;

import com.notes.entity.EmbedBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmbedBlockRepository extends JpaRepository<EmbedBlock, Long> {
    
    Optional<EmbedBlock> findByUidAndDeletedFalse(String uid);
    
    List<EmbedBlock> findByNoteUidAndDeletedFalse(String noteUid);
}



