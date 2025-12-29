package com.notes.service.impl;

import com.notes.dto.response.FileUploadDTO;
import com.notes.entity.FileRecord;
import com.notes.exception.BusinessException;
import com.notes.repository.FileRecordRepository;
import com.notes.service.FileService;
import com.notes.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    
    private final FileRecordRepository fileRecordRepository;
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedTypes;
    
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;  // 10MB
    private static final int TARGET_WIDTH = 1920;  // 压缩后最大宽度
    
    @Override
    public FileUploadDTO uploadFile(MultipartFile file, String noteUid) {
        // 验证文件类型
        String contentType = file.getContentType();
        List<String> allowed = Arrays.asList(allowedTypes.split(","));
        if (!allowed.contains(contentType)) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }
        
        try {
            // 计算文件哈希
            String fileHash = calculateFileHash(file.getBytes());
            
            // 检查是否已存在相同文件
            FileRecord existing = fileRecordRepository.findByFileHashAndDeletedFalse(fileHash).orElse(null);
            if (existing != null) {
                return createUploadDTO(existing);
            }
            
            // 生成存储路径
            LocalDate now = LocalDate.now();
            String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String storedName = UUID.randomUUID().toString() + "." + extension;
            String relativePath = "/files/" + yearMonth + "/" + storedName;
            
            // 创建目录
            Path dirPath = Paths.get(uploadPath, yearMonth);
            Files.createDirectories(dirPath);
            
            Path filePath = dirPath.resolve(storedName);
            
            // 如果是图片且大于阈值，进行压缩
            if (contentType.startsWith("image/") && file.getSize() > MAX_IMAGE_SIZE) {
                compressImage(file, filePath.toFile());
            } else {
                file.transferTo(filePath);
            }
            
            // 保存文件记录
            FileRecord record = new FileRecord();
            record.setUid(UidGenerator.generateFileUid());
            record.setNoteUid(noteUid);
            record.setOriginalName(file.getOriginalFilename());
            record.setStoredName(storedName);
            record.setFilePath(relativePath);
            record.setFileSize(Files.size(filePath));
            record.setMimeType(contentType);
            record.setFileHash(fileHash);
            
            record = fileRecordRepository.save(record);
            
            return createUploadDTO(record);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    private void compressImage(MultipartFile file, File dest) throws IOException {
        Thumbnails.of(file.getInputStream())
                .width(TARGET_WIDTH)
                .keepAspectRatio(true)
                .outputQuality(0.85)
                .toFile(dest);
    }
    
    private String calculateFileHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算文件哈希失败", e);
        }
    }
    
    private FileUploadDTO createUploadDTO(FileRecord record) {
        FileUploadDTO dto = new FileUploadDTO();
        dto.setUid(record.getUid());
        dto.setFileName(record.getOriginalName());
        dto.setFilePath(record.getFilePath());
        dto.setFileSize(record.getFileSize());
        dto.setMimeType(record.getMimeType());
        return dto;
    }
}



