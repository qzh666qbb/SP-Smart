-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    course_code VARCHAR(32) NOT NULL COMMENT '课程编码',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_type VARCHAR(20) NOT NULL COMMENT 'ONE_TO_ONE、SMALL_CLASS、GROUP',
    default_duration_minutes INT UNSIGNED NOT NULL COMMENT '默认时长',
    default_consume_hours DECIMAL(8,2) NOT NULL COMMENT '默认消耗课时',
    description VARCHAR(2000) COMMENT '课程说明',
    status VARCHAR(20) NOT NULL COMMENT 'ENABLED、DISABLED',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_course_code (course_code),
    INDEX idx_course_name (course_name),
    INDEX idx_status (status),
    UNIQUE uk_course_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程定义';

-- 班级表
CREATE TABLE IF NOT EXISTS class_group (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    class_code VARCHAR(32) NOT NULL COMMENT '班级编号',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '关联课程',
    primary_teacher_id BIGINT UNSIGNED COMMENT '主授教师',
    max_students INT UNSIGNED COMMENT '最大人数',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE、INACTIVE',
    start_date DATE COMMENT '开班日期',
    end_date DATE COMMENT '结班日期',
    remark VARCHAR(1000) COMMENT '备注',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_class_code (class_code),
    INDEX idx_course_id (course_id),
    INDEX idx_primary_teacher_id (primary_teacher_id),
    INDEX idx_status (status),
    UNIQUE uk_class_code (class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级';

-- 班级学员关系表
CREATE TABLE IF NOT EXISTS class_group_student (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    class_group_id BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学员ID',
    joined_at DATETIME NOT NULL COMMENT '加入班级时间',
    left_at DATETIME COMMENT '离开班级时间',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE、LEFT',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_class_group_student (class_group_id, student_id),
    INDEX idx_student_id_status (student_id, status),
    INDEX idx_class_group_id_status (class_group_id, status),
    UNIQUE uk_class_group_student (class_group_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级学员关系';

-- 周期排课规则表
CREATE TABLE IF NOT EXISTS schedule_series (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    series_name VARCHAR(100) COMMENT '排课计划名称',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    class_group_id BIGINT UNSIGNED COMMENT '班级ID',
    teacher_id BIGINT UNSIGNED NOT NULL COMMENT '授课教师',
    recurrence_type VARCHAR(20) NOT NULL COMMENT 'NONE、WEEKLY',
    weekdays VARCHAR(32) COMMENT '星期配置，如6或2,4',
    start_date DATE NOT NULL COMMENT '计划开始日期',
    end_date DATE COMMENT '计划结束日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    consume_hours DECIMAL(8,2) NOT NULL COMMENT '默认消耗课时',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE、PAUSED、ENDED',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_teacher_id_date (teacher_id, start_date, end_date),
    INDEX idx_class_group_id (class_group_id),
    INDEX idx_course_id (course_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='周期排课规则';

-- 实际课程表
CREATE TABLE IF NOT EXISTS lesson (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    lesson_no VARCHAR(40) NOT NULL COMMENT '课程实例编号',
    schedule_series_id BIGINT UNSIGNED COMMENT '来源周期计划',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    class_group_id BIGINT UNSIGNED COMMENT '班级ID',
    teacher_id BIGINT UNSIGNED NOT NULL COMMENT '授课教师',
    lesson_date DATE NOT NULL COMMENT '上课日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    status VARCHAR(20) NOT NULL COMMENT 'PENDING、IN_PROGRESS、COMPLETED、CANCELLED',
    location VARCHAR(255) COMMENT '上课地点',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    completed_at DATETIME COMMENT '完成时间',
    completed_by BIGINT UNSIGNED COMMENT '完成人',
    source_type VARCHAR(20) NOT NULL COMMENT 'MANUAL、SERIES、IMPORT',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_lesson_no (lesson_no),
    INDEX idx_teacher_date_time (teacher_id, lesson_date, start_time),
    INDEX idx_class_group_date (class_group_id, lesson_date),
    INDEX idx_schedule_series_id (schedule_series_id),
    INDEX idx_status_date (status, lesson_date),
    UNIQUE uk_lesson_no (lesson_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实际课程实例';

-- 课程学员表
CREATE TABLE IF NOT EXISTS lesson_student (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    lesson_id BIGINT UNSIGNED NOT NULL COMMENT '课程实例ID',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学员ID',
    attendance_status VARCHAR(20) NOT NULL COMMENT 'PENDING、PRESENT、LEAVE、ABSENT、CANCELLED',
    sign_in_at DATETIME COMMENT '签到时间',
    consume_hours DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '实际消耗课时',
    should_consume TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要扣课时',
    hour_transaction_id BIGINT UNSIGNED COMMENT '对应课时流水',
    attendance_remark VARCHAR(500) COMMENT '到课备注',
    confirmed_by BIGINT UNSIGNED COMMENT '状态确认人',
    confirmed_at DATETIME COMMENT '状态确认时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_lesson_student (lesson_id, student_id),
    INDEX idx_student_id_status (student_id, attendance_status),
    INDEX idx_lesson_id_status (lesson_id, attendance_status),
    INDEX idx_hour_transaction_id (hour_transaction_id),
    UNIQUE uk_lesson_student (lesson_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程学员状态';