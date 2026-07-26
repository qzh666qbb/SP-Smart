-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    audit_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    module VARCHAR(50) NOT NULL COMMENT '模块名称',
    target_id BIGINT UNSIGNED COMMENT '目标ID',
    target_type VARCHAR(50) COMMENT '目标类型',
    operator_id BIGINT UNSIGNED NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人名称',
    operator_role VARCHAR(20) COMMENT '操作人角色',
    before_value JSON COMMENT '操作前数据',
    after_value JSON COMMENT '操作后数据',
    change_summary VARCHAR(500) COMMENT '变更摘要',
    ip_address VARCHAR(50) COMMENT '操作IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    success TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功',
    error_message VARCHAR(2000) COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_module_target (module, target_id),
    INDEX idx_operator_date (operator_id, created_at),
    INDEX idx_audit_type_date (audit_type, created_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审计日志';
