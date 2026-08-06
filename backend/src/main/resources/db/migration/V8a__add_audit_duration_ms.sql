-- V8a: audit_log 补充 duration_ms 列（审计日志记录接口耗时）
ALTER TABLE audit_log 
    ADD COLUMN duration_ms INT UNSIGNED COMMENT '接口耗时（毫秒）' AFTER error_message;

-- V8b: 也补充 operator_name 虽然列已在 V7 有，但统一校验;
-- 此处留空，仅做版本占位
