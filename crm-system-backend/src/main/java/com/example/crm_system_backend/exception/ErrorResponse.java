package com.example.crm_system_backend.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {

    public String message;
    public HttpStatus status;


    /**
     * Constructs an instance of ErrorResponse with the specified message and HTTP status.
     *
     * @param message the error message describing the issue encountered
     * @param status the HTTP status code associated with the error
     */
    public ErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}