package com.notes.repository;

import com.notes.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByUidAndDeletedFalse(String uid);
    
    Optional<Tag> findByNameAndDeletedFalse(String name);
    
    List<Tag> findByDeletedFalseOrderByUsageCountDesc();
    
    List<Tag> findByUidInAndDeletedFalse(List<String> uids);
    
    boolean existsByNameAndDeletedFalse(String name);
}



