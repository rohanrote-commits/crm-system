package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;

public class ReportException extends RuntimeException {

    ErrorCode errorCode;

    public ReportException(ErrorCode message) {
        super(message.getMessage());
        this.errorCode = message;
    }
}
