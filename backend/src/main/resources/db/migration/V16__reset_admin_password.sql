-- 重置管理员初始密码为 123456（BCrypt $2a$10）
-- 原 V1 插入的 admin 账号密码未知（猜解失败），因此通过 Flyway 强制重置。
-- 仅当 username='admin' 且 status='ENABLED' 且 role='ADMIN' 时生效，避免误伤其他账号。
UPDATE sys_user
SET password_hash = '$2a$10$IhOvZ5LlfxEZsPql3w/Dh.9JJysRkYzOh4fvEuOWT2hVUvQccfmfa',
    password_updated_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE username = 'admin'
  AND role = 'ADMIN'
  AND status = 'ENABLED';
