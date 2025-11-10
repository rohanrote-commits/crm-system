package com.example.crm_system_backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
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


    @ExceptionHandler(ExcelProcessingError.class)
    public ResponseEntity<byte[]> handleExcelProcessingError(ExcelProcessingError exception){

        return  ResponseEntity.internalServerError().header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_OCTET_STREAM_VALUE).body(exception.file);
    }

    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<ErrorResponse> handleFileDownloadException(FileDownloadException exception){
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getErrorCode().getMessage(),
                exception.getErrorCode().getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    @ExceptionHandler(LeadException.class)
    public ResponseEntity<ErrorResponse> handleLeadException(LeadException exception){
        ErrorResponse errorResponse = new ErrorResponse(
                exception.errorCode.getMessage(),
                exception.errorCode.getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}
