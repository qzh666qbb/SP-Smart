-- V8: 新增课包、课时类型拆分、课程评分、到期时间等字段

-- 1. 课包表
CREATE TABLE IF NOT EXISTS hour_package (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    package_code VARCHAR(32) NOT NULL COMMENT '课包编码',
    package_name VARCHAR(100) NOT NULL COMMENT '课包名称',
    package_type VARCHAR(20) NOT NULL COMMENT 'PAID、GIFT、EXPERIENCE、ACTIVITY',
    total_hours DECIMAL(10,2) NOT NULL COMMENT '总课时数',
    price DECIMAL(10,2) COMMENT '价格',
    valid_months INT UNSIGNED NOT NULL DEFAULT 12 COMMENT '有效期月数',
    description VARCHAR(500) COMMENT '描述',
    status VARCHAR(20) NOT NULL COMMENT 'ENABLED、DISABLED',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT UNSIGNED NOT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_package_code (package_code),
    INDEX idx_package_type (package_type),
    INDEX idx_status (status),
    UNIQUE uk_package_code (package_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课包';

-- 2. 学员课包购买记录
CREATE TABLE IF NOT EXISTS student_package_record (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学员ID',
    package_id BIGINT UNSIGNED NOT NULL COMMENT '课包ID',
    purchase_date DATE NOT NULL COMMENT '购买日期',
    expire_date DATE NOT NULL COMMENT '到期日期',
    total_hours DECIMAL(10,2) NOT NULL COMMENT '购买课时数',
    remaining_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '剩余课时',
    consumed_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已消耗课时',
    refunded_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已退课时',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE、EXPIRED、REFUNDED',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_student_id (student_id),
    INDEX idx_package_id (package_id),
    INDEX idx_status (status),
    INDEX idx_expire_date (expire_date),
    INDEX idx_student_status (student_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学员课包购买记录';

-- 3. 扩展学员课时账户表，增加4种课时拆分字段
ALTER TABLE student_hour_account 
    ADD COLUMN paid_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '付费课时' AFTER remaining_hours,
    ADD COLUMN gift_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '赠课时' AFTER paid_hours,
    ADD COLUMN experience_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '体验课时' AFTER gift_hours,
    ADD COLUMN activity_hours DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '活动课时' AFTER experience_hours;

-- 4. 扩展课时流水表，增加课时来源和课包关联
ALTER TABLE hour_transaction 
    ADD COLUMN hour_source VARCHAR(20) COMMENT '课时来源：PAID、GIFT、EXPERIENCE、ACTIVITY' AFTER transaction_type,
    ADD COLUMN package_record_id BIGINT UNSIGNED COMMENT '关联课包记录ID' AFTER lesson_student_id,
    ADD COLUMN expire_date DATE COMMENT '到期日期（充值时写入）' AFTER idempotency_key;

-- 5. 扩展课程学员表，增加综合评分
ALTER TABLE lesson_student 
    ADD COLUMN rating DECIMAL(2,1) COMMENT '课程综合评分(1-5分)' AFTER attendance_remark,
    ADD COLUMN rating_comment VARCHAR(500) COMMENT '评分备注' AFTER rating;

-- 6. 扩展学员表，增加身高体重快照
ALTER TABLE student 
    ADD COLUMN height DECIMAL(5,1) COMMENT '身高(cm)' AFTER phone,
    ADD COLUMN weight DECIMAL(5,1) COMMENT '体重(kg)' AFTER height;

-- 7. 教练考勤表
CREATE TABLE IF NOT EXISTS teacher_attendance (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    teacher_id BIGINT UNSIGNED NOT NULL COMMENT '教师ID',
    lesson_id BIGINT UNSIGNED COMMENT '关联课程',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    attendance_status VARCHAR(20) NOT NULL COMMENT 'PRESENT、LEAVE、ABSENT',
    check_in_time TIME COMMENT '签到时间',
    check_out_time TIME COMMENT '签退时间',
    leave_reason VARCHAR(500) COMMENT '请假原因',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_teacher_date (teacher_id, attendance_date),
    INDEX idx_lesson_id (lesson_id),
    INDEX idx_status (attendance_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教练考勤';

-- 8. 体测评分标准扩展：增加score值字段
ALTER TABLE physical_score_standard 
    ADD COLUMN score_value DECIMAL(5,1) COMMENT '对应分数值(1-5)' AFTER level;

-- 9. 增加5级枚举值说明更新
-- physical_score_standard.level 扩展支持 TERRIBLE、INADEQUATE、PASS、GOOD、EXCELLENT
