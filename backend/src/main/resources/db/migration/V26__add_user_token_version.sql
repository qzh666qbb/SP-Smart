ALTER TABLE sys_user
    ADD COLUMN token_version INT NOT NULL DEFAULT 0 COMMENT '刷新令牌版本，退出登录时递增' AFTER password_updated_at;
