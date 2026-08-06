package com.example.training.common.infrastructure.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final JdbcTemplate jdbcTemplate;

    @Async
    public void createLog(String auditType, String module, Long targetId, String targetType,
                          Long operatorId, String operatorRole,
                          String beforeValue, String afterValue,
                          boolean success, String errorMessage, long durationMs) {
        try {
            String sql = """
                INSERT INTO audit_log (audit_type, module, target_id, target_type,
                    operator_id, operator_role, before_value, after_value,
                    success, error_message, duration_ms, ip_address, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            jdbcTemplate.update(sql,
                    auditType, module, targetId, targetType,
                    operatorId, operatorRole,
                    beforeValue, afterValue,
                    success ? 1 : 0, errorMessage, (int) durationMs,
                    resolveClientIp(), LocalDateTime.now());
        } catch (Exception e) {
            log.warn("审计日志写入失败: {}", e.getMessage());
        }
    }

    private String resolveClientIp() {
        return "127.0.0.1";
    }
}
