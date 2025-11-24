package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;

public class ErrorRecordException extends RuntimeException{

    ErrorCode errorCode;

    /**
     * Constructs a new ErrorRecordException with the specified ErrorCode.
     *
     * @param errorCode the error code representing the specific error condition.
     */
    public ErrorRecordException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }
}
