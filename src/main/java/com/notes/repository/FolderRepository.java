package com.notes.repository;

import com.notes.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    
    Optional<Folder> findByUidAndDeletedFalse(String uid);
    
    List<Folder> findByParentUidAndDeletedFalseOrderBySortOrderAsc(String parentUid);
    
    List<Folder> findByParentUidIsNullAndDeletedFalseOrderBySortOrderAsc();
    
    List<Folder> findByDeletedFalseOrderByPathAsc();
    
    @Query("SELECT f FROM Folder f WHERE f.path LIKE :pathPrefix% AND f.deleted = false")
    List<Folder> findByPathStartingWith(String pathPrefix);
    
    boolean existsByUidAndDeletedFalse(String uid);
}



