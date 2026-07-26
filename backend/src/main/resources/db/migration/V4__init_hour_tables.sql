-- 学员课时账户表
CREATE TABLE IF NOT EXISTS student_hour_account (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学员ID',
    total_recharged DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '累计增加课时',
    total_consumed DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '累计消耗课时',
    total_refunded DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '累计退回或减少课时',
    remaining_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '当前剩余课时',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_id (student_id),
    UNIQUE uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学员课时账户';

-- 课时流水表
CREATE TABLE IF NOT EXISTS hour_transaction (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    transaction_no VARCHAR(40) NOT NULL COMMENT '流水号',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学员ID',
    transaction_type VARCHAR(20) NOT NULL COMMENT 'RECHARGE、CONSUME、REFUND、ADJUST、ROLLBACK、IMPORT',
    change_amount DECIMAL(10,2) NOT NULL COMMENT '变动数量，增加为正，扣减为负',
    before_balance DECIMAL(10,2) NOT NULL COMMENT '变动前余额',
    after_balance DECIMAL(10,2) NOT NULL COMMENT '变动后余额',
    lesson_id BIGINT UNSIGNED COMMENT '关联课程',
    lesson_student_id BIGINT UNSIGNED COMMENT '关联学员上课记录',
    related_transaction_id BIGINT UNSIGNED COMMENT '被撤销或关联的原流水',
    reason VARCHAR(500) NOT NULL COMMENT '变动原因',
    operator_id BIGINT UNSIGNED NOT NULL COMMENT '操作用户',
    operator_role VARCHAR(20) NOT NULL COMMENT 'ADMIN、TEACHER',
    idempotency_key VARCHAR(100) COMMENT '幂等键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_student_id_date (student_id, created_at),
    INDEX idx_lesson_id (lesson_id),
    INDEX idx_lesson_student_id (lesson_student_id),
    INDEX idx_operator_id_date (operator_id, created_at),
    INDEX idx_transaction_type_date (transaction_type, created_at),
    UNIQUE uk_transaction_no (transaction_no),
    UNIQUE uk_idempotency_key (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课时变动流水';