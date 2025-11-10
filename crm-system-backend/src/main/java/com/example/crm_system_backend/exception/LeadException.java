package com.example.crm_system_backend.exception;

public class LeadException extends RuntimeException{

    ErrorCode errorCode;
    public LeadException(ErrorCode message) {
        super(message.name());
        this.errorCode = message;
    }
}
