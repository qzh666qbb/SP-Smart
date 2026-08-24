-- V10: 统一修正 所有业务表的 created_by/updated_by 默认值 以及 补齐缺失的审计列
-- 核心修复：很多 V1~V9 建表脚本里压根没有 created_by/updated_by 列（如 hour_transaction、student_hour_account、
-- class_group_student、schedule_series、report_task、file_upload_record、audit_log 等）。
-- 本脚本使用存储过程动态判断列是否存在：
--   - 若列不存在 → ADD COLUMN 并设置 DEFAULT 0
--   - 若列已存在 → MODIFY 调整为 NOT NULL DEFAULT 0
-- 避免 "Unknown column" / "Table doesn't exist" 报错。

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_ensure_audit_column $$
CREATE PROCEDURE sp_ensure_audit_column(
    IN p_table  VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_after  VARCHAR(64)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO v_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = p_table
      AND COLUMN_NAME  = p_column;
    IF v_exists = 0 THEN
        IF p_after IS NULL OR p_after = '' THEN
            SET @_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column,
                               '` BIGINT UNSIGNED NOT NULL DEFAULT 0');
        ELSE
            SET @_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column,
                               '` BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER `', p_after, '`');
        END IF;
        PREPARE stmt FROM @_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    ELSE
        SET @_sql = CONCAT('ALTER TABLE `', p_table, '` MODIFY COLUMN `', p_column,
                           '` BIGINT UNSIGNED NOT NULL DEFAULT 0');
        PREPARE stmt FROM @_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS sp_add_column_if_not_exists $$
CREATE PROCEDURE sp_add_column_if_not_exists(
    IN p_table  VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_def    VARCHAR(255),
    IN p_after  VARCHAR(64)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO v_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = p_table
      AND COLUMN_NAME  = p_column;
    IF v_exists = 0 THEN
        IF p_after IS NULL OR p_after = '' THEN
            SET @_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def);
        ELSE
            SET @_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def,
                               ' AFTER `', p_after, '`');
        END IF;
        PREPARE stmt FROM @_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS sp_table_exists $$
CREATE FUNCTION sp_table_exists(p_table VARCHAR(64)) RETURNS TINYINT
DETERMINISTIC
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO v_exists
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table;
    RETURN v_exists;
END $$

DELIMITER ;

-- ============ 为所有业务表确保 created_by / updated_by 两列 (不存在则ADD，已存在则MODIFY DEFAULT 0) ============
-- (A) 真正"业务+操作者写入"的表：必须保证 created_by / updated_by 均 BIGINT UNSIGNED DEFAULT 0
--     参数: (table, column, after)

-- V1 用户体系
CALL sp_ensure_audit_column('sys_user',              'created_by', 'status');
CALL sp_ensure_audit_column('sys_user',              'updated_by', 'created_by');
CALL sp_ensure_audit_column('teacher_profile',       'created_by', NULL);
CALL sp_ensure_audit_column('teacher_profile',       'updated_by', 'created_by');
CALL sp_ensure_audit_column('parent_profile',        'created_by', NULL);
CALL sp_ensure_audit_column('parent_profile',        'updated_by', 'created_by');

-- V2 学生/家长关系
CALL sp_ensure_audit_column('student',               'created_by', NULL);
CALL sp_ensure_audit_column('student',               'updated_by', 'created_by');
CALL sp_ensure_audit_column('parent_student_relation','created_by', NULL);
CALL sp_ensure_audit_column('parent_student_relation','updated_by', 'created_by');

-- V3 课程/班级/排课/出勤
CALL sp_ensure_audit_column('course',                'created_by', NULL);
CALL sp_ensure_audit_column('course',                'updated_by', 'created_by');
CALL sp_ensure_audit_column('class_group',           'created_by', NULL);
CALL sp_ensure_audit_column('class_group',           'updated_by', 'created_by');
CALL sp_ensure_audit_column('class_group_student',   'created_by', NULL);
CALL sp_ensure_audit_column('class_group_student',   'updated_by', 'created_by');
CALL sp_ensure_audit_column('schedule_series',       'created_by', NULL);
CALL sp_ensure_audit_column('schedule_series',       'updated_by', 'created_by');
CALL sp_ensure_audit_column('lesson',                'created_by', NULL);
CALL sp_ensure_audit_column('lesson',                'updated_by', 'created_by');
CALL sp_ensure_audit_column('lesson_student',        'created_by', NULL);
CALL sp_ensure_audit_column('lesson_student',        'updated_by', 'created_by');

-- V4 课时账户/流水
CALL sp_ensure_audit_column('student_hour_account',  'created_by', NULL);
CALL sp_ensure_audit_column('student_hour_account',  'updated_by', 'created_by');
CALL sp_ensure_audit_column('hour_transaction',      'created_by', NULL);
CALL sp_ensure_audit_column('hour_transaction',      'updated_by', 'created_by');

-- V5 体测 (physical_score_detail / physical_template_item / physical_test_record 原本没写 audit 列)
CALL sp_ensure_audit_column('physical_test_item',      'created_by', NULL);
CALL sp_ensure_audit_column('physical_test_item',      'updated_by', 'created_by');
CALL sp_ensure_audit_column('physical_score_standard', 'created_by', NULL);
CALL sp_ensure_audit_column('physical_score_standard', 'updated_by', 'created_by');
CALL sp_ensure_audit_column('physical_test_template',  'created_by', NULL);
CALL sp_ensure_audit_column('physical_test_template',  'updated_by', 'created_by');
CALL sp_ensure_audit_column('physical_template_item',  'created_by', NULL);
CALL sp_ensure_audit_column('physical_template_item',  'updated_by', 'created_by');
CALL sp_ensure_audit_column('physical_test_batch',     'created_by', NULL);
CALL sp_ensure_audit_column('physical_test_batch',     'updated_by', 'created_by');
CALL sp_ensure_audit_column('physical_test_record',    'created_by', 'remark');
CALL sp_ensure_audit_column('physical_test_record',    'updated_by', 'created_by');
CALL sp_ensure_audit_column('physical_score_detail',   'created_by', 'remark');
CALL sp_ensure_audit_column('physical_score_detail',   'updated_by', 'created_by');

-- V6 报告/上传
CALL sp_ensure_audit_column('report_task',           'created_by', NULL);
CALL sp_ensure_audit_column('report_task',           'updated_by', 'created_by');
CALL sp_ensure_audit_column('file_upload_record',    'created_by', NULL);
CALL sp_ensure_audit_column('file_upload_record',    'updated_by', 'created_by');

-- V7 审计日志
CALL sp_ensure_audit_column('audit_log',             'created_by', NULL);
CALL sp_ensure_audit_column('audit_log',             'updated_by', 'created_by');

-- V8 课包/购课记录/教练考勤
CALL sp_ensure_audit_column('hour_package',          'created_by', NULL);
CALL sp_ensure_audit_column('hour_package',          'updated_by', 'created_by');
CALL sp_ensure_audit_column('student_package_record','created_by', NULL);
CALL sp_ensure_audit_column('student_package_record','updated_by', 'created_by');
CALL sp_ensure_audit_column('teacher_attendance',    'created_by', NULL);
CALL sp_ensure_audit_column('teacher_attendance',    'updated_by', 'created_by');

-- ============ 补齐缺失的时间列 (created_at / updated_at / deleted_at) ============
CALL sp_add_column_if_not_exists('physical_test_record', 'created_at', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',                       'updated_by');
CALL sp_add_column_if_not_exists('physical_test_record', 'updated_at', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');
CALL sp_add_column_if_not_exists('physical_test_record', 'deleted_at', 'DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('physical_score_detail','created_at', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',                       'updated_by');
CALL sp_add_column_if_not_exists('physical_score_detail','updated_at', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');
CALL sp_add_column_if_not_exists('physical_score_detail','deleted_at', 'DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('physical_template_item','created_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',                       'updated_by');
CALL sp_add_column_if_not_exists('physical_template_item','updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('student_package_record','updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('class_group',           'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('class_group_student',   'created_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',                       'updated_by');
CALL sp_add_column_if_not_exists('class_group_student',   'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');
CALL sp_add_column_if_not_exists('class_group_student',   'deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('parent_student_relation','updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');
CALL sp_add_column_if_not_exists('parent_student_relation','deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('hour_transaction',      'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('teacher_profile',       'created_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',                       'created_by');
CALL sp_add_column_if_not_exists('teacher_profile',       'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('parent_profile',        'created_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',                       'created_by');
CALL sp_add_column_if_not_exists('parent_profile',        'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('report_task',           'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('teacher_attendance',    'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('file_upload_record',    'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');

CALL sp_add_column_if_not_exists('schedule_series',       'created_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',                       'created_by');
CALL sp_add_column_if_not_exists('schedule_series',       'updated_at','DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'created_at');
CALL sp_add_column_if_not_exists('schedule_series',       'deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('student_hour_account',  'deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('lesson',                'deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('course',                'deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('student',               'deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('teacher_attendance',    'deleted_at','DATETIME',                                                          'updated_at');

CALL sp_add_column_if_not_exists('sys_user',              'deleted_at','DATETIME',                                                          'updated_at');

-- 清理辅助对象
DROP FUNCTION IF EXISTS sp_table_exists;
DROP PROCEDURE IF EXISTS sp_ensure_audit_column;
DROP PROCEDURE IF EXISTS sp_add_column_if_not_exists;
