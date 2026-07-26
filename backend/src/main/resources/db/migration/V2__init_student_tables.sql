-- 学员表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_no VARCHAR(32) NOT NULL COMMENT '学员编号',
    name VARCHAR(64) NOT NULL COMMENT '学员姓名',
    gender VARCHAR(10) COMMENT 'MALE、FEMALE、UNKNOWN',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '学员联系电话',
    primary_teacher_id BIGINT UNSIGNED COMMENT '主要负责教师',
    enrollment_date DATE COMMENT '入学日期',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE、SUSPENDED、GRADUATED',
    medical_notes VARCHAR(1000) COMMENT '运动注意事项',
    remark VARCHAR(1000) COMMENT '备注',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_student_no (student_no),
    INDEX idx_name (name),
    INDEX idx_primary_teacher_id (primary_teacher_id),
    INDEX idx_status (status),
    INDEX idx_birth_date (birth_date),
    UNIQUE uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学员资料';

-- 家长学员关系表
CREATE TABLE IF NOT EXISTS parent_student_relation (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    parent_id BIGINT UNSIGNED NOT NULL COMMENT '家长资料ID',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学员ID',
    relation_type VARCHAR(20) COMMENT 'FATHER、MOTHER、GUARDIAN、OTHER',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主要联系人',
    bind_status VARCHAR(20) NOT NULL COMMENT 'PENDING、CONFIRMED、UNBOUND',
    bound_at DATETIME COMMENT '绑定时间',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id_student_id (parent_id, student_id),
    INDEX idx_student_id_bind_status (student_id, bind_status),
    INDEX idx_parent_id_bind_status (parent_id, bind_status),
    UNIQUE uk_parent_student (parent_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家长与学员绑定关系';