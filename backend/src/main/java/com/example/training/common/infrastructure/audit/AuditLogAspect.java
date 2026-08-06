package com.example.training.common.infrastructure.audit;

import com.example.training.common.security.CurrentUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @Around("execution(* com.example.training..*Controller.*(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        String httpMethod = resolveHttpMethod(method);
        boolean isWrite = "POST".equals(httpMethod) || "PUT".equals(httpMethod) || "DELETE".equals(httpMethod) || "PATCH".equals(httpMethod);

        Object result = null;
        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            if (isWrite) {
                try {
                    long duration = System.currentTimeMillis() - startTime;
                    Long operatorId = resolveOperatorId();
                    String operatorRole = resolveOperatorRole();

                    auditLogService.createLog(
                            resolveAuditType(className, methodName),
                            resolveModule(className),
                            null, null,
                            operatorId, operatorRole,
                            serializeArgs(joinPoint.getArgs()),
                            result != null ? serializeResult(result) : null,
                            success, errorMessage, duration);
                } catch (Exception e) {
                    log.warn("审计日志记录失败: {}", e.getMessage());
                }
            }
        }
    }

    private String resolveHttpMethod(Method method) {
        if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class)) return "DELETE";
        if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PatchMapping.class)) return "PATCH";
        return "GET";
    }

    private Long resolveOperatorId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CurrentUser user) {
                return user.getUserId();
            }
        } catch (Exception e) {}
        return 0L;
    }

    private String resolveOperatorRole() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CurrentUser user) {
                return user.getRole();
            }
        } catch (Exception e) {}
        return "SYSTEM";
    }

    private String resolveAuditType(String className, String methodName) {
        if (methodName.contains("create") || methodName.contains("add")) return "CREATE";
        if (methodName.contains("update") || methodName.contains("edit")) return "UPDATE";
        if (methodName.contains("delete") || methodName.contains("remove")) return "DELETE";
        if (methodName.contains("login")) return "LOGIN";
        return "ACTION";
    }

    private String resolveModule(String className) {
        if (className.contains("Course")) return "COURSE";
        if (className.contains("Student")) return "STUDENT";
        if (className.contains("Schedule")) return "SCHEDULE";
        if (className.contains("Hour")) return "HOUR";
        if (className.contains("Physical")) return "PHYSICAL_TEST";
        if (className.contains("Report")) return "REPORT";
        if (className.contains("User")) return "USER";
        return "SYSTEM";
    }

    private String serializeArgs(Object[] args) {
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            return "[args serialization failed]";
        }
    }

    private String serializeResult(Object result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "[result serialization failed]";
        }
    }
}
