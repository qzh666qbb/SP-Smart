package com.example.training.common.exception;

import com.example.training.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getDetailMessage());
        HttpStatus status = resolveHttpStatus(e.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getCode(), e.getDetailMessage()));
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case RESOURCE_NOT_FOUND, LESSON_NOT_FOUND, SCHEDULE_NOT_FOUND,
                 STUDENT_NOT_FOUND, COURSE_NOT_FOUND, CLASS_GROUP_NOT_FOUND,
                 PHYSICAL_TEST_NOT_FOUND, USER_NOT_FOUND, FILE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case AUTH_INVALID_TOKEN, AUTH_TOKEN_EXPIRED, AUTH_UNAUTHORIZED,
                 AUTH_INVALID_CREDENTIALS, AUTH_ACCOUNT_DISABLED -> HttpStatus.UNAUTHORIZED;
            case AUTH_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case OPERATION_NOT_ALLOWED, LESSON_NOT_PENDING, HOUR_INSUFFICIENT,
                 HOUR_NEGATIVE_NOT_ALLOWED, HOUR_TRANSACTION_FAILED,
                 COURSE_DISABLED, CLASS_GROUP_NOT_ACTIVE, STUDENT_BIND_PENDING,
                 PARAM_VALID_ERROR, USER_INVALID_PHONE, IMPORT_FAILED,
                 EXPORT_FAILED, REPORT_GENERATE_FAILED, DUPLICATE_RESOURCE,
                 FILE_UPLOAD_ERROR,
                 USER_PHONE_ALREADY_EXISTS, USER_WEAK_PASSWORD,
                 TEACHER_APPLY_PENDING, TEACHER_APPLY_NOT_FOUND, TEACHER_PHONE_NOT_PRE_REGISTERED,
                 WECHAT_CODE_EXCHANGE_FAILED,
                 CHILD_MAX_LIMIT -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(ErrorCode.PARAM_VALID_ERROR.getCode())
                        .message(ErrorCode.PARAM_VALID_ERROR.getMessage())
                        .data(errors)
                        .build());
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_INVALID_CREDENTIALS.getCode(), 
                        ErrorCode.AUTH_INVALID_CREDENTIALS.getMessage()));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_UNAUTHORIZED.getCode(),
                        ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException e) {
        log.warn("授权拦截: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.AUTH_FORBIDDEN.getCode(),
                        ErrorCode.AUTH_FORBIDDEN.getMessage()));
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(org.springframework.dao.DuplicateKeyException e) {
        log.warn("数据唯一键冲突", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.DUPLICATE_RESOURCE.getCode(), "账号、手机号或编号已存在，请检查后重试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("系统内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), 
                        ErrorCode.SYSTEM_ERROR.getMessage()));
    }
}
