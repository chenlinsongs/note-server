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
    
    Page<Note> findByDeletedFalseOrderByIsPinnedDescSortOrderDesc(Pageable pageable);
    
    Page<Note> findByFolderUidAndDeletedFalseOrderByIsPinnedDescSortOrderDesc(String folderUid, Pageable pageable);
    
    List<Note> findByDeletedFalse();
    
    @Query("SELECT COUNT(n) FROM Note n WHERE n.folderUid = :folderUid AND n.deleted = false")
    Integer countByFolderUid(String folderUid);
    
    boolean existsByUidAndDeletedFalse(String uid);
    
    // 数据库搜索（Elasticsearch 不可用时的备选方案）
    @Query("SELECT n FROM Note n WHERE n.deleted = false AND (n.title LIKE %:keyword% OR n.contentText LIKE %:keyword%) ORDER BY n.updatedAt DESC")
    Page<Note> searchByKeyword(String keyword, Pageable pageable);
    
    @Query("SELECT n FROM Note n WHERE n.deleted = false AND n.folderUid = :folderUid AND (n.title LIKE %:keyword% OR n.contentText LIKE %:keyword%) ORDER BY n.updatedAt DESC")
    Page<Note> searchByKeywordAndFolder(String keyword, String folderUid, Pageable pageable);
    
    // 垃圾桶相关
    Page<Note> findByDeletedTrueOrderByDeletedAtDesc(Pageable pageable);
    
    Optional<Note> findByUidAndDeletedTrue(String uid);
    
    @Query("SELECT COUNT(n) FROM Note n WHERE n.deleted = true")
    long countDeleted();
    
    // 按文件夹和类型查询
    List<Note> findByFolderUidAndNoteTypeAndDeletedFalseOrderByIsPinnedDescSortOrderDesc(String folderUid, String noteType);
    
    List<Note> findByFolderUidAndDeletedFalseOrderByIsPinnedDescSortOrderDesc(String folderUid);
    
    // 获取文件夹中最大的 sortOrder
    @Query("SELECT MAX(n.sortOrder) FROM Note n WHERE n.folderUid = :folderUid")
    Long findMaxSortOrderByFolderUid(String folderUid);
    
    // 获取全局最大的 sortOrder
    @Query("SELECT MAX(n.sortOrder) FROM Note n")
    Long findMaxSortOrder();
}

