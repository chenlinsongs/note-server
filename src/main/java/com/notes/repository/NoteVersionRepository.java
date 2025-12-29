package com.notes.repository;

import com.notes.entity.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {
    
    List<NoteVersion> findByNoteUidOrderByVersionDesc(String noteUid);
    
    Optional<NoteVersion> findByNoteUidAndVersion(String noteUid, Integer version);
    
    @Query("SELECT nv FROM NoteVersion nv WHERE nv.noteUid = :noteUid AND nv.isSnapshot = true AND nv.version <= :version ORDER BY nv.version DESC LIMIT 1")
    Optional<NoteVersion> findLatestSnapshotBeforeVersion(String noteUid, Integer version);
    
    @Query("SELECT nv FROM NoteVersion nv WHERE nv.noteUid = :noteUid AND nv.version > :startVersion AND nv.version <= :endVersion ORDER BY nv.version ASC")
    List<NoteVersion> findVersionsBetween(String noteUid, Integer startVersion, Integer endVersion);
    
    @Query("SELECT MAX(nv.version) FROM NoteVersion nv WHERE nv.noteUid = :noteUid")
    Integer findMaxVersionByNoteUid(String noteUid);
}



