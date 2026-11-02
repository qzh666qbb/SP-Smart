ALTER TABLE schedule_series
    ADD COLUMN assistant_teacher_id_1 BIGINT UNSIGNED NULL COMMENT '助教1' AFTER teacher_id,
    ADD COLUMN assistant_teacher_id_2 BIGINT UNSIGNED NULL COMMENT '助教2' AFTER assistant_teacher_id_1,
    ADD INDEX idx_assistant_teacher_1_date (assistant_teacher_id_1, start_date, end_date),
    ADD INDEX idx_assistant_teacher_2_date (assistant_teacher_id_2, start_date, end_date);

ALTER TABLE lesson
    ADD COLUMN assistant_teacher_id_1 BIGINT UNSIGNED NULL COMMENT '助教1' AFTER teacher_id,
    ADD COLUMN assistant_teacher_id_2 BIGINT UNSIGNED NULL COMMENT '助教2' AFTER assistant_teacher_id_1,
    ADD INDEX idx_assistant_teacher_1_date (assistant_teacher_id_1, lesson_date, start_time),
    ADD INDEX idx_assistant_teacher_2_date (assistant_teacher_id_2, lesson_date, start_time);
