ALTER TABLE sys_user
    ADD COLUMN nickname VARCHAR(64) NULL COMMENT '用户昵称' AFTER phone,
    ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT '用户头像地址' AFTER nickname;
