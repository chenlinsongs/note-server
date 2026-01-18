-- 笔记应用数据库初始化脚本
-- 数据库: notes_db
-- 字符集: utf8mb4

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS notes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE notes_db;

-- ========================================
-- 1. 文件夹表 (folders)
-- ========================================
CREATE TABLE IF NOT EXISTS folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    uid VARCHAR(64) NOT NULL COMMENT '业务主键',
    parent_uid VARCHAR(64) DEFAULT NULL COMMENT '父文件夹UID',
    name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件夹名称',
    path VARCHAR(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '/' COMMENT '完整路径',
    level INT DEFAULT 0 COMMENT '层级深度',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '软删除标记：0-正常，1-已删除',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    
    UNIQUE KEY uk_uid (uid),
    INDEX idx_parent_uid (parent_uid),
    INDEX idx_path (path(255)),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件夹表';

-- ========================================
-- 2. 笔记表 (notes)
-- ========================================
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    uid VARCHAR(64) NOT NULL COMMENT '业务主键',
    folder_uid VARCHAR(64) DEFAULT NULL COMMENT '所属文件夹UID',
    title VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '笔记标题',
    note_type VARCHAR(20) DEFAULT 'document' COMMENT '笔记类型：document/spreadsheet/mindmap/canvas',
    content JSON COMMENT '笔记内容（TipTap JSON格式或表格JSON）',
    content_text MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '纯文本内容（用于搜索）',
    content_markdown MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Markdown格式（用于导出）',
    word_count INT DEFAULT 0 COMMENT '字数统计',
    version INT DEFAULT 1 COMMENT '当前版本号',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    sort_order BIGINT DEFAULT NULL COMMENT '排序顺序（按创建时间戳）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '软删除标记：0-正常，1-已删除',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    
    UNIQUE KEY uk_uid (uid),
    INDEX idx_folder_uid (folder_uid),
    INDEX idx_note_type (note_type),
    INDEX idx_sort_order (sort_order),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记表';

-- ========================================
-- 3. 笔记版本表 (note_versions)
-- ========================================
CREATE TABLE IF NOT EXISTS note_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    uid VARCHAR(64) NOT NULL COMMENT '业务主键',
    note_uid VARCHAR(64) NOT NULL COMMENT '笔记UID',
    version INT NOT NULL COMMENT '版本号',
    is_snapshot TINYINT(1) DEFAULT 0 COMMENT '是否为完整快照',
    patch_data JSON COMMENT '差异数据（JSON Patch格式）',
    snapshot_data JSON COMMENT '完整快照数据（当is_snapshot=1时）',
    content_hash VARCHAR(64) COMMENT '内容哈希（用于校验）',
    change_summary VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '变更摘要',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_uid (uid),
    UNIQUE KEY uk_note_version (note_uid, version),
    INDEX idx_note_uid (note_uid),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记版本表';

-- ========================================
-- 4. 文件记录表 (file_records)
-- ========================================
CREATE TABLE IF NOT EXISTS file_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    uid VARCHAR(64) NOT NULL COMMENT '业务主键',
    note_uid VARCHAR(64) DEFAULT NULL COMMENT '关联笔记UID',
    original_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '原始文件名',
    stored_name VARCHAR(255) COMMENT '存储文件名',
    file_path VARCHAR(500) COMMENT '文件相对路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    file_hash VARCHAR(64) COMMENT '文件哈希（用于去重）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '软删除标记：0-正常，1-已删除',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    
    UNIQUE KEY uk_uid (uid),
    INDEX idx_note_uid (note_uid),
    INDEX idx_file_hash (file_hash),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件记录表';

-- ========================================
-- 5. 标签表 (tags)
-- ========================================
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    uid VARCHAR(64) NOT NULL COMMENT '业务主键',
    name VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#1890ff' COMMENT '标签颜色',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '软删除标记：0-正常，1-已删除',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    
    UNIQUE KEY uk_uid (uid),
    UNIQUE KEY uk_name (name),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';

-- ========================================
-- 6. 笔记标签关联表 (note_tags)
-- ========================================
CREATE TABLE IF NOT EXISTS note_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    note_uid VARCHAR(64) NOT NULL COMMENT '笔记UID',
    tag_uid VARCHAR(64) NOT NULL COMMENT '标签UID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_note_tag (note_uid, tag_uid),
    INDEX idx_note_uid (note_uid),
    INDEX idx_tag_uid (tag_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记标签关联表';

-- ========================================
-- 7. 嵌入块表 (embed_blocks)
-- ========================================
CREATE TABLE IF NOT EXISTS embed_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    uid VARCHAR(64) NOT NULL COMMENT '业务主键（blockUid）',
    note_uid VARCHAR(64) NOT NULL COMMENT '所属笔记UID',
    block_type VARCHAR(50) NOT NULL COMMENT '块类型：chart/canvas/mindmap等',
    data TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '块数据（JSON字符串）',
    version INT DEFAULT 1 COMMENT '当前版本号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '软删除标记：0-正常，1-已删除',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    
    UNIQUE KEY uk_uid (uid),
    INDEX idx_note_uid (note_uid),
    INDEX idx_block_type (block_type),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='嵌入块表';

-- ========================================
-- 8. 嵌入块版本表 (embed_block_versions)
-- ========================================
CREATE TABLE IF NOT EXISTS embed_block_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    uid VARCHAR(64) NOT NULL COMMENT '业务主键',
    block_uid VARCHAR(64) NOT NULL COMMENT '嵌入块UID',
    version INT NOT NULL COMMENT '版本号',
    is_snapshot TINYINT(1) DEFAULT 0 COMMENT '是否为完整快照',
    patch_data TEXT COMMENT '差异数据',
    snapshot_data TEXT COMMENT '完整快照数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_uid (uid),
    UNIQUE KEY uk_block_version (block_uid, version),
    INDEX idx_block_uid (block_uid),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='嵌入块版本表';

-- ========================================
-- 初始化数据
-- ========================================

-- 创建默认文件夹
INSERT INTO folders (uid, name, path, level, sort_order) VALUES
('fld_default_inbox', '收件箱', '/fld_default_inbox', 0, 0),
('fld_default_work', '工作笔记', '/fld_default_work', 0, 1),
('fld_default_personal', '个人笔记', '/fld_default_personal', 0, 2)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 创建默认标签
INSERT INTO tags (uid, name, color) VALUES
('tag_important', '重要', '#ef4444'),
('tag_todo', '待办', '#f59e0b'),
('tag_done', '已完成', '#10b981'),
('tag_idea', '灵感', '#8b5cf6'),
('tag_reference', '参考', '#3b82f6')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库表创建完成！' AS message;
