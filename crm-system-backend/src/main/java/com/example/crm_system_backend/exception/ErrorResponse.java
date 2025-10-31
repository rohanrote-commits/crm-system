package com.example.crm_system_backend.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {
    public String message;
    public HttpStatus status;

    public ErrorResponse(String message, String code,HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}