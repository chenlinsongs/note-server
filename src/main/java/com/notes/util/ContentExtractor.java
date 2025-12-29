package com.notes.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * 内容提取工具类
 * 从 TipTap JSON 中提取纯文本
 */
@Component
public class ContentExtractor {
    
    private final ObjectMapper objectMapper;
    
    public ContentExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 从 TipTap JSON 提取纯文本
     */
    public String extractText(String jsonContent) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return "";
        }
        
        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            StringBuilder sb = new StringBuilder();
            extractTextFromNode(root, sb);
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }
    
    private void extractTextFromNode(JsonNode node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        
        // 如果是文本节点
        if (node.has("type") && "text".equals(node.get("type").asText())) {
            if (node.has("text")) {
                sb.append(node.get("text").asText());
            }
            return;
        }
        
        // 递归处理 content 数组
        if (node.has("content") && node.get("content").isArray()) {
            for (JsonNode child : node.get("content")) {
                extractTextFromNode(child, sb);
            }
            
            // 段落之间添加换行
            String type = node.has("type") ? node.get("type").asText() : "";
            if ("paragraph".equals(type) || "heading".equals(type)) {
                sb.append("\n");
            }
        }
    }
    
    /**
     * 计算字数
     */
    public int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 移除空白字符后计算长度
        return text.replaceAll("\\s+", "").length();
    }
}



