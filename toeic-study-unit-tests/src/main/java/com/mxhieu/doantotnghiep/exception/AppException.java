package com.mxhieu.doantotnghiep.exception;

import lombok.Getter;

/**
 * Custom runtime exception dùng trong toàn bộ ứng dụng.
 * Mang theo ErrorCode để GlobalExceptionHandler trả về HTTP response phù hợp.
 */
@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
