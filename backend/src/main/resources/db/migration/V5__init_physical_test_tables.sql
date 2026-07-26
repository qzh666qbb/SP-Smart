-- 体测指标定义表
CREATE TABLE IF NOT EXISTS physical_test_item (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    item_code VARCHAR(32) NOT NULL COMMENT '指标编码',
    item_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    item_type VARCHAR(20) NOT NULL COMMENT 'HEIGHT、WEIGHT、SPEED、POWER、ENDURANCE、FLEXIBILITY、SKILL',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    gender_specific TINYINT NOT NULL DEFAULT 0 COMMENT '是否区分性别',
    age_min INT COMMENT '适用最小年龄',
    age_max INT COMMENT '适用最大年龄',
    description VARCHAR(500) COMMENT '指标说明',
    status VARCHAR(20) NOT NULL COMMENT 'ENABLED、DISABLED',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_item_code (item_code),
    INDEX idx_item_type (item_type),
    INDEX idx_status (status),
    UNIQUE uk_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体测指标定义';

-- 体测评分标准表
CREATE TABLE IF NOT EXISTS physical_score_standard (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '指标ID',
    gender VARCHAR(10) COMMENT '性别，ALL表示通用',
    age_group VARCHAR(20) COMMENT '年龄段，如6-7',
    min_value DECIMAL(10,2) NOT NULL COMMENT '最小值',
    max_value DECIMAL(10,2) NOT NULL COMMENT '最大值',
    score INT NOT NULL COMMENT '对应分数',
    level VARCHAR(10) COMMENT '评级：EXCELLENT、GOOD、PASS、FAIL',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_item_gender_age (item_id, gender, age_group),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体测评分标准';

-- 体测模板表
CREATE TABLE IF NOT EXISTS physical_test_template (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(32) NOT NULL COMMENT '模板编码',
    target_gender VARCHAR(10) COMMENT '适用性别，ALL表示通用',
    min_age INT COMMENT '最小年龄',
    max_age INT COMMENT '最大年龄',
    description VARCHAR(500) COMMENT '模板说明',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE、INACTIVE',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_template_code (template_code),
    INDEX idx_status (status),
    UNIQUE uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体测模板';

-- 体测模板指标关联表
CREATE TABLE IF NOT EXISTS physical_template_item (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    template_id BIGINT UNSIGNED NOT NULL COMMENT '模板ID',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '指标ID',
    required TINYINT NOT NULL DEFAULT 1 COMMENT '是否必填',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_template_item (template_id, item_id),
    INDEX idx_item_id (item_id),
    UNIQUE uk_template_item (template_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体测模板指标关联';

-- 体测批次表
CREATE TABLE IF NOT EXISTS physical_test_batch (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    batch_name VARCHAR(100) NOT NULL COMMENT '批次名称',
    batch_code VARCHAR(32) NOT NULL COMMENT '批次编码',
    template_id BIGINT UNSIGNED NOT NULL COMMENT '使用模板',
    test_date DATE NOT NULL COMMENT '测试日期',
    location VARCHAR(255) COMMENT '测试地点',
    status VARCHAR(20) NOT NULL COMMENT 'PENDING、IN_PROGRESS、COMPLETED、CANCELLED',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_batch_code (batch_code),
    INDEX idx_template_id (template_id),
    INDEX idx_status (status),
    UNIQUE uk_batch_code (batch_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体测批次';

-- 体测结果记录表
CREATE TABLE IF NOT EXISTS physical_test_record (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    record_no VARCHAR(40) NOT NULL COMMENT '记录编号',
    batch_id BIGINT UNSIGNED NOT NULL COMMENT '批次ID',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学员ID',
    test_date DATE NOT NULL COMMENT '测试日期',
    tester_id BIGINT UNSIGNED COMMENT '测试人',
    total_score DECIMAL(5,1) COMMENT '总分',
    overall_level VARCHAR(10) COMMENT '综合评级',
    status VARCHAR(20) NOT NULL COMMENT 'DRAFT、SUBMITTED、CONFIRMED',
    remark VARCHAR(1000) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_no (record_no),
    INDEX idx_batch_student (batch_id, student_id),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status),
    UNIQUE uk_record_no (record_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体测结果记录';

-- 体测单项成绩表
CREATE TABLE IF NOT EXISTS physical_score_detail (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    record_id BIGINT UNSIGNED NOT NULL COMMENT '体测记录ID',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '指标ID',
    raw_value DECIMAL(10,2) COMMENT '原始成绩',
    converted_score DECIMAL(5,1) COMMENT '转换得分',
    level VARCHAR(10) COMMENT '评级',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_item (record_id, item_id),
    INDEX idx_item_id (item_id),
    UNIQUE uk_record_item (record_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体测单项成绩';
