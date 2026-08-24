-- V15: lesson 表补齐 consume_hours（消课时数）列 + deleted_at（软删除）列
-- 直接内联判断列是否存在，不依赖外部存储过程
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_v15_add_col $$
CREATE PROCEDURE sp_v15_add_col(
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
        SET @_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def,
                           IF(p_after IS NULL OR p_after = '', '', CONCAT(' AFTER `', p_after, '`')));
        PREPARE stmt FROM @_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

CALL sp_v15_add_col('lesson', 'consume_hours', 'DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT ''实际消课时数''', 'source_type') $$
CALL sp_v15_add_col('lesson', 'deleted_at',    'DATETIME NULL COMMENT ''软删除时间''',              'updated_at') $$

DROP PROCEDURE IF EXISTS sp_v15_add_col $$
DELIMITER ;
