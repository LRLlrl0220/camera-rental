package com.shiguang.camera.common;

//状态码枚举
public enum ResultCode {
    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),
    
    // 业务相关
    CAPTCHA_ERROR(1001, "验证码错误"),
    CAPTCHA_EXPIRED(1002, "验证码已过期"),
    USER_EXISTS(1003, "用户已存在"),
    USER_NOT_FOUND(1004, "用户不存在"),
    PASSWORD_ERROR(1005, "密码错误"),
    REAL_NAME_REQUIRED(1006, "请先进行实名认证"),
    DEVICE_UNAVAILABLE(1007, "设备不可用");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}