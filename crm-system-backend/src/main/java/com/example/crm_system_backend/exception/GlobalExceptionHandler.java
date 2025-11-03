package com.example.crm_system_backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException userException){
        ErrorResponse errorResponse = new ErrorResponse(
                userException.errorCode.getMessage(),
                userException.errorCode.getStatus()
        );

        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<ErrorResponse> handleFileDownloadException(FileDownloadException exception){
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getErrorCode().getMessage(),
                exception.getErrorCode().getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}
