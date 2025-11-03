package com.example.crm_system_backend.exception;

public class LeadException extends RuntimeException{

    public LeadException(ErrorCode message) {
        super(message.name());
    }
}
