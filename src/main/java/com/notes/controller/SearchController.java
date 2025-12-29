package com.notes.controller;

import com.notes.dto.response.ApiResponse;
import com.notes.dto.response.SearchResultDTO;
import com.notes.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    @GetMapping
    public ApiResponse<Page<SearchResultDTO>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String folderUid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(searchService.search(keyword, folderUid, pageable));
    }
    
    @PostMapping("/reindex")
    public ApiResponse<Void> reindexAll() {
        searchService.reindexAll();
        return ApiResponse.success("重建索引完成", null);
    }
}



