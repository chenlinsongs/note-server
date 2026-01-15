package com.notes.controller;

import com.notes.dto.request.NoteRequest;
import com.notes.dto.response.ApiResponse;
import com.notes.dto.response.NoteDTO;
import com.notes.dto.response.NoteListDTO;
import com.notes.dto.response.NoteVersionDTO;
import com.notes.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final NoteService noteService;
    
    @GetMapping
    public ApiResponse<Page<NoteListDTO>> getNotes(
            @RequestParam(required = false) String folderUid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(noteService.getNotes(folderUid, pageable));
    }
    
    @GetMapping("/{uid}")
    public ApiResponse<NoteDTO> getNote(@PathVariable String uid) {
        return ApiResponse.success(noteService.getNote(uid));
    }
    
    @PostMapping
    public ApiResponse<NoteDTO> createNote(@Valid @RequestBody NoteRequest request) {
        return ApiResponse.success("笔记创建成功", noteService.createNote(request));
    }
    
    @PutMapping("/{uid}")
    public ApiResponse<NoteDTO> updateNote(@PathVariable String uid, @Valid @RequestBody NoteRequest request) {
        return ApiResponse.success("笔记更新成功", noteService.updateNote(uid, request));
    }
    
    @DeleteMapping("/{uid}")
    public ApiResponse<Void> deleteNote(@PathVariable String uid) {
        noteService.deleteNote(uid);
        return ApiResponse.success("笔记删除成功", null);
    }
    
    @GetMapping("/{uid}/versions")
    public ApiResponse<List<NoteVersionDTO>> getVersions(@PathVariable String uid) {
        return ApiResponse.success(noteService.getVersions(uid));
    }
    
    @GetMapping("/{uid}/versions/{version}")
    public ApiResponse<NoteDTO> getVersion(@PathVariable String uid, @PathVariable Integer version) {
        return ApiResponse.success(noteService.getVersion(uid, version));
    }
    
    @PostMapping("/{uid}/restore/{version}")
    public ApiResponse<NoteDTO> restoreVersion(@PathVariable String uid, @PathVariable Integer version) {
        return ApiResponse.success("版本恢复成功", noteService.restoreVersion(uid, version));
    }
    
    // ================= 垃圾桶相关接口 =================
    
    @GetMapping("/trash/list")
    public ApiResponse<Page<NoteListDTO>> getDeletedNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(noteService.getDeletedNotes(pageable));
    }
    
    @PostMapping("/trash/{uid}/restore")
    public ApiResponse<Void> restoreNote(@PathVariable String uid) {
        noteService.restoreNote(uid);
        return ApiResponse.success("笔记已恢复", null);
    }
    
    @DeleteMapping("/trash/{uid}")
    public ApiResponse<Void> permanentlyDeleteNote(@PathVariable String uid) {
        noteService.permanentlyDeleteNote(uid);
        return ApiResponse.success("笔记已永久删除", null);
    }
    
    @GetMapping("/trash/count")
    public ApiResponse<Long> getDeletedCount() {
        return ApiResponse.success(noteService.getDeletedCount());
    }
    
    @GetMapping("/trash/{uid}")
    public ApiResponse<NoteDTO> getDeletedNote(@PathVariable String uid) {
        return ApiResponse.success(noteService.getDeletedNote(uid));
    }
}



