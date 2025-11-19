package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;

public class UploadHistoryException extends RuntimeException {

    ErrorCode errorCode;
    public UploadHistoryException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
