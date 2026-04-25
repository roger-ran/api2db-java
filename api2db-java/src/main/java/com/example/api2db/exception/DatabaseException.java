package com.example.api2db.exception;

/**
 * 数据库异常
 * 当数据库操作失败时抛出
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}