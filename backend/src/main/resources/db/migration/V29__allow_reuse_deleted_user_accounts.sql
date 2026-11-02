-- 仅未删除账号占用用户名和手机号，保留历史账号信息。
ALTER TABLE sys_user
    ADD COLUMN active_username VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN username ELSE NULL END) STORED,
    ADD COLUMN active_phone VARCHAR(32) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN phone ELSE NULL END) STORED,
    ADD UNIQUE INDEX uk_active_username (active_username),
    ADD UNIQUE INDEX uk_active_phone (active_phone),
    DROP INDEX uk_username,
    DROP INDEX uk_sys_user_phone;
