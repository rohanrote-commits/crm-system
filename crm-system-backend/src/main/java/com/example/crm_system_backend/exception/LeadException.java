package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;

public class LeadException extends RuntimeException{

    ErrorCode errorCode;
    public LeadException(ErrorCode message) {
        super(message.getMessage());
        this.errorCode = errorCode;
    }
}
