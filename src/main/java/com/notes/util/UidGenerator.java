package com.notes.util;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * 业务ID生成器
 * 规则：前缀 + 时间戳 + 随机字符串
 */
public class UidGenerator {
    
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    public static String generateFolderUid() {
        return generateUid("fld_");
    }
    
    public static String generateNoteUid() {
        return generateUid("note_");
    }
    
    public static String generateNoteVersionUid() {
        return generateUid("nver_");
    }
    
    public static String generateFileUid() {
        return generateUid("file_");
    }
    
    public static String generateTagUid() {
        return generateUid("tag_");
    }
    
    public static String generateBlockUid() {
        return generateUid("blk_");
    }
    
    public static String generateBlockVersionUid() {
        return generateUid("bver_");
    }
    
    private static String generateUid(String prefix) {
        long timestamp = Instant.now().getEpochSecond();
        String randomPart = generateRandomString(8);
        return prefix + timestamp + "_" + randomPart;
    }
    
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}



