package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;

public class ErrorRecordException extends RuntimeException{

    ErrorCode errorCode;
    public ErrorRecordException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }
}
