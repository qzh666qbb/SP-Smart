-- 教师申请表
CREATE TABLE IF NOT EXISTS teacher_apply (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    phone VARCHAR(20) NOT NULL COMMENT '申请手机号',
    real_name VARCHAR(64) NOT NULL COMMENT '教师真实姓名',
    wechat_openid VARCHAR(128) NULL COMMENT '微信OpenID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    reviewer_id BIGINT UNSIGNED NULL COMMENT '审核人ID',
    review_comment VARCHAR(200) NULL COMMENT '审核备注',
    reviewed_at DATETIME NULL COMMENT '审核时间',
    created_by BIGINT UNSIGNED NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE uk_teacher_apply_phone (phone),
    INDEX idx_teacher_apply_status (status),
    INDEX idx_teacher_apply_wechat_openid (wechat_openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师申请审核表';

-- 教师预登记表（管理端提前登记哪些手机号允许作为教师申请）
CREATE TABLE IF NOT EXISTS teacher_pre_register (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    real_name VARCHAR(64) NOT NULL COMMENT '教师真实姓名',
    created_by BIGINT UNSIGNED NOT NULL COMMENT '登记人ID(管理员)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE uk_teacher_pre_register_phone (phone),
    INDEX idx_teacher_pre_register_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师手机号预登记表';

-- ===== 去重 sys_user.phone 重复 =====
-- 历史数据中可能存在多账号共享 phone（如 student01/student02/student03/student04 共享 '13800000000'，以及 admin 同号）
-- 规则：保留 phone 相同组中创建最早的 (id 最小) 作为主账号；其他账号 phone 置为 CONCAT('dup_', phone, '_', id) 以便保留账号可用
UPDATE sys_user u
INNER JOIN (
    SELECT phone, MIN(id) AS keep_id
    FROM sys_user
    WHERE phone IS NOT NULL AND phone <> ''
    GROUP BY phone
    HAVING COUNT(*) > 1
) dup ON u.phone = dup.phone AND u.id <> dup.keep_id
SET u.phone = CONCAT('dup_', u.phone, '_', u.id);

-- 将空字符串 phone 统一置为 NULL，保证 UNIQUE INDEX 允许多个账号未填手机号
UPDATE sys_user SET phone = NULL WHERE phone = '';

-- 给 sys_user.phone 增加唯一索引（如果不存在）
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_phone ON sys_user(phone);
