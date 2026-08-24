-- V11: 学员表补家长联系信息字段，用于家长端精确匹配绑定 (防止家长浏览全量学员)
-- 绑定流程：家长提交 (孩子学号, 孩子姓名, 家长手机号后4位 或 家长手机号全号) 进行匹配，匹配成功才能绑定。
-- 这里补 parent_phone 与 parent_name 字段（原先只在 student 表的 mock 里有，没在 SQL schema 里）
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_add_col_if_not_exists_v11 $$
CREATE PROCEDURE sp_add_col_if_not_exists_v11(
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

-- student 表补家长联系信息（用于家长端绑定精确匹配）
CALL sp_add_col_if_not_exists_v11('student', 'parent_name',   'VARCHAR(64)  COMMENT ''家长姓名''',                           'phone');
CALL sp_add_col_if_not_exists_v11('student', 'parent_phone',  'VARCHAR(20)  COMMENT ''家长联系电话''',                          'parent_name');
CALL sp_add_col_if_not_exists_v11('student', 'parent_relation','VARCHAR(20)  COMMENT ''家长关系 FATHER/MOTHER/GUARDIAN/OTHER''',   'parent_phone');

-- 加索引，用于家长手机号匹配绑定
SET @idx_stu_parent_phone = IFNULL((SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'student' AND INDEX_NAME = 'idx_parent_phone'), 0);
SET @idx_stu_parent_phone = IF(@idx_stu_parent_phone = 0, 'ALTER TABLE student ADD INDEX idx_parent_phone (parent_phone)', 'SELECT 1');
PREPARE stmt FROM @idx_stu_parent_phone; EXECUTE stmt; DEALLOCATE PREPARE stmt;

DROP PROCEDURE IF EXISTS sp_add_col_if_not_exists_v11;
