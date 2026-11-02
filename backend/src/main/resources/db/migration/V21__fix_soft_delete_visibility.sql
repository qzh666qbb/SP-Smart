-- V21: 修复软删除数据仍占用/仍可见的问题
-- 1. physical_test_record 补 deleted_at 字段，后端删除体测记录时写入该字段
-- 2. 已软删除学员释放 student_no 唯一键，允许重新录入相同学号

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_add_col_if_not_exists_v21 $$
CREATE PROCEDURE sp_add_col_if_not_exists_v21(
    IN p_table  VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_def    VARCHAR(255),
    IN p_after  VARCHAR(64)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO v_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column;
    IF v_exists = 0 THEN
        IF p_after IS NULL OR p_after = '' THEN
            SET @_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def);
        ELSE
            SET @_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def, ' AFTER `', p_after, '`');
        END IF;
        PREPARE stmt FROM @_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

CALL sp_add_col_if_not_exists_v21(
    'physical_test_record',
    'deleted_at',
    'DATETIME NULL COMMENT ''软删除时间''',
    'updated_at'
);

DROP PROCEDURE IF EXISTS sp_add_col_if_not_exists_v21;

-- 释放已软删除学员的 student_no，避免重新录入相同学号时触发 uk_student_no。
UPDATE student
SET student_no = CONCAT(
    'DEL_',
    id,
    '_',
    LEFT(student_no, GREATEST(0, 32 - CHAR_LENGTH(CONCAT('DEL_', id, '_'))))
)
WHERE deleted_at IS NOT NULL
  AND student_no NOT LIKE 'DEL\_%' ESCAPE '\\';
