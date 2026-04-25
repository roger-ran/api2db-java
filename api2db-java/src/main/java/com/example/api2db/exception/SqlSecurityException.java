package com.example.api2db.exception;

/**
 * SQL 安全异常
 * 当 SQL 语句违反白名单或黑名单规则时抛出
 */
public class SqlSecurityException extends RuntimeException {

    public SqlSecurityException(String message) {
        super(message);
    }

    public SqlSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}