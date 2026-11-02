CREATE TABLE IF NOT EXISTS schedule_series_student (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    schedule_series_id BIGINT UNSIGNED NOT NULL,
    student_id BIGINT UNSIGNED NOT NULL,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_series_student (schedule_series_id, student_id),
    KEY idx_schedule_series_student_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课系列学员关系';

-- 兼容已有数据：从已生成的课次名单中恢复系列学员关系。
INSERT IGNORE INTO schedule_series_student (schedule_series_id, student_id, created_by)
SELECT DISTINCT l.schedule_series_id, ls.student_id, l.created_by
FROM lesson l
JOIN lesson_student ls ON ls.lesson_id = l.id
WHERE l.schedule_series_id IS NOT NULL
  AND l.deleted_at IS NULL;
