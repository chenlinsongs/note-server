package com.notes.controller;

import com.notes.dto.request.TagRequest;
import com.notes.dto.response.ApiResponse;
import com.notes.dto.response.TagDTO;
import com.notes.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @GetMapping
    public ApiResponse<List<TagDTO>> getAllTags() {
        return ApiResponse.success(tagService.getAllTags());
    }
    
    @PostMapping
    public ApiResponse<TagDTO> createTag(@Valid @RequestBody TagRequest request) {
        return ApiResponse.success("标签创建成功", tagService.createTag(request));
    }
    
    @PutMapping("/{uid}")
    public ApiResponse<TagDTO> updateTag(@PathVariable String uid, @Valid @RequestBody TagRequest request) {
        return ApiResponse.success("标签更新成功", tagService.updateTag(uid, request));
    }
    
    @DeleteMapping("/{uid}")
    public ApiResponse<Void> deleteTag(@PathVariable String uid) {
        tagService.deleteTag(uid);
        return ApiResponse.success("标签删除成功", null);
    }
}

