package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;
import lombok.Data;

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
