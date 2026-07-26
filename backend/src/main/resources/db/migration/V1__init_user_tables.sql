-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(64) COMMENT '用户名，管理员PC登录使用',
    password_hash VARCHAR(255) COMMENT '密码哈希',
    role VARCHAR(20) NOT NULL COMMENT 'ADMIN、TEACHER、PARENT',
    phone VARCHAR(20) COMMENT '手机号',
    wechat_openid VARCHAR(128) COMMENT '微信小程序OpenID',
    wechat_unionid VARCHAR(128) COMMENT '微信UnionID',
    status VARCHAR(20) NOT NULL COMMENT 'ENABLED、DISABLED、LOCKED',
    last_login_at DATETIME COMMENT '最后登录时间',
    password_updated_at DATETIME COMMENT '密码更新时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_username (username),
    INDEX idx_wechat_openid (wechat_openid),
    INDEX idx_phone (phone),
    INDEX idx_role_status (role, status),
    UNIQUE uk_username (username),
    UNIQUE uk_wechat_openid (wechat_openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录账号';

-- 教师资料表
CREATE TABLE IF NOT EXISTS teacher_profile (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '关联用户表',
    teacher_no VARCHAR(32) NOT NULL COMMENT '教师编号',
    name VARCHAR(64) NOT NULL COMMENT '教师姓名',
    gender VARCHAR(10) COMMENT 'MALE、FEMALE、UNKNOWN',
    avatar_url VARCHAR(500) COMMENT '头像地址',
    introduction VARCHAR(1000) COMMENT '教师介绍',
    employment_status VARCHAR(20) NOT NULL COMMENT 'ACTIVE、INACTIVE',
    joined_date DATE COMMENT '入职日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    UNIQUE uk_user_id (user_id),
    UNIQUE uk_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师资料';

-- 家长资料表
CREATE TABLE IF NOT EXISTS parent_profile (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '关联用户表',
    name VARCHAR(64) COMMENT '家长姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系手机号',
    relation_remark VARCHAR(64) COMMENT '默认关系说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_user_id (user_id),
    INDEX idx_phone (phone),
    UNIQUE uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家长资料';

-- 创建初始管理员账号
INSERT INTO sys_user (username, password_hash, role, phone, status) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'ADMIN', '13800000000', 'ENABLED')
ON DUPLICATE KEY UPDATE username=username;