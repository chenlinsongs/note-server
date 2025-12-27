package com.notes.controller;

import com.notes.dto.request.BlockRequest;
import com.notes.dto.response.ApiResponse;
import com.notes.dto.response.BlockDTO;
import com.notes.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {
    
    private final BlockService blockService;
    
    @GetMapping("/{uid}")
    public ApiResponse<BlockDTO> getBlock(@PathVariable String uid) {
        return ApiResponse.success(blockService.getBlock(uid));
    }
    
    @PostMapping
    public ApiResponse<BlockDTO> createBlock(@Valid @RequestBody BlockRequest request) {
        return ApiResponse.success("嵌入块创建成功", blockService.createBlock(request));
    }
    
    @PutMapping("/{uid}")
    public ApiResponse<BlockDTO> updateBlock(@PathVariable String uid, @RequestBody Map<String, String> body) {
        String data = body.get("data");
        return ApiResponse.success("嵌入块更新成功", blockService.updateBlock(uid, data));
    }
    
    @DeleteMapping("/{uid}")
    public ApiResponse<Void> deleteBlock(@PathVariable String uid) {
        blockService.deleteBlock(uid);
        return ApiResponse.success("嵌入块删除成功", null);
    }
}

