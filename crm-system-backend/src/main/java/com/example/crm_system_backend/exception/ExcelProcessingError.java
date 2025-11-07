package com.example.crm_system_backend.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ExcelProcessingError extends RuntimeException{
    public ErrorCode errorCode;
    public  byte[] file;
    public ExcelProcessingError(ErrorCode errorCode, byte[] file){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.file = file;

    }
}
