package com.example.api2db.exception;

/**
 * 认证异常
 * 当 API key 或连接 ID 验证失败时抛出
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}