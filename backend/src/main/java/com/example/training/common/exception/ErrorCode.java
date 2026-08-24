package com.example.training.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    SUCCESS(0, "success"),
    
    SYSTEM_ERROR(10000, "系统内部错误"),
    PARAM_VALID_ERROR(10001, "参数校验失败"),
    RESOURCE_NOT_FOUND(10002, "资源不存在"),
    DUPLICATE_RESOURCE(10003, "资源已存在"),
    OPERATION_NOT_ALLOWED(10004, "操作不允许"),
    
    AUTH_INVALID_TOKEN(20001, "无效的认证令牌"),
    AUTH_TOKEN_EXPIRED(20002, "认证令牌已过期"),
    AUTH_UNAUTHORIZED(20003, "未授权访问"),
    AUTH_FORBIDDEN(20004, "禁止访问"),
    AUTH_INVALID_CREDENTIALS(20005, "用户名或密码错误"),
    AUTH_ACCOUNT_DISABLED(20006, "账户已禁用"),
    
    USER_NOT_FOUND(30001, "用户不存在"),
    USER_INVALID_PHONE(30002, "手机号格式不正确"),
    USER_PHONE_ALREADY_EXISTS(30003, "手机号已注册"),
    USER_WEAK_PASSWORD(30004, "密码过短，至少6位"),

    TEACHER_APPLY_PENDING(30101, "教师申请正在审核中"),
    TEACHER_APPLY_NOT_FOUND(30102, "教师申请不存在"),
    TEACHER_PHONE_NOT_PRE_REGISTERED(30103, "该手机号未被管理端提前登记为教师，请先联系管理员"),

    WECHAT_CODE_EXCHANGE_FAILED(30201, "微信登录临时凭证无效"),

    CHILD_MAX_LIMIT(30301, "一个家长最多允许绑定 6 个孩子"),
    
    STUDENT_NOT_FOUND(40001, "学员不存在"),
    STUDENT_BIND_PENDING(40002, "绑定请求已存在"),
    
    COURSE_NOT_FOUND(50001, "课程不存在"),
    COURSE_DISABLED(50002, "课程已禁用"),
    
    CLASS_GROUP_NOT_FOUND(60001, "班级不存在"),
    CLASS_GROUP_NOT_ACTIVE(60002, "班级已停止"),
    
    SCHEDULE_NOT_FOUND(70001, "排课不存在"),
    
    LESSON_NOT_FOUND(80001, "课程实例不存在"),
    LESSON_NOT_PENDING(80002, "课程状态不允许此操作"),
    
    HOUR_INSUFFICIENT(90001, "课时不足"),
    HOUR_NEGATIVE_NOT_ALLOWED(90002, "不允许负余额"),
    HOUR_TRANSACTION_FAILED(90003, "课时变动失败"),
    
    PHYSICAL_TEST_NOT_FOUND(100001, "体测记录不存在"),
    
    FILE_UPLOAD_ERROR(110001, "文件上传失败"),
    FILE_NOT_FOUND(110002, "文件不存在"),
    
    IMPORT_FAILED(120001, "导入失败"),
    EXPORT_FAILED(120002, "导出失败"),
    
    REPORT_GENERATE_FAILED(130001, "报表生成失败");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
