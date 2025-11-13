package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;

public class UserException extends RuntimeException{
    ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
