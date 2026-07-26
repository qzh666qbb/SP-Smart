-- 报表任务表
CREATE TABLE IF NOT EXISTS report_task (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    report_type VARCHAR(50) NOT NULL COMMENT '报表类型',
    report_name VARCHAR(100) NOT NULL COMMENT '报表名称',
    params JSON COMMENT '报表参数',
    status VARCHAR(20) NOT NULL COMMENT 'PENDING、GENERATING、COMPLETED、FAILED',
    file_path VARCHAR(500) COMMENT '生成文件路径',
    file_name VARCHAR(200) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小',
    error_message VARCHAR(2000) COMMENT '错误信息',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at DATETIME COMMENT '完成时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_report_type (report_type),
    INDEX idx_status_date (status, created_at),
    INDEX idx_created_by_date (created_by, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报表任务';

-- 文件上传记录表
CREATE TABLE IF NOT EXISTS file_upload_record (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    file_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(50) COMMENT '文件类型',
    md5_hash VARCHAR(64) COMMENT '文件MD5',
    upload_type VARCHAR(20) COMMENT 'AVATAR、DOCUMENT、REPORT、IMPORT',
    reference_id BIGINT UNSIGNED COMMENT '关联业务ID',
    reference_type VARCHAR(50) COMMENT '关联业务类型',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '上传人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_reference (reference_id, reference_type),
    INDEX idx_upload_type_date (upload_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件上传记录';
