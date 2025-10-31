package com.example.crm_system_backend.exception;

public class UserException extends RuntimeException{
    ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
