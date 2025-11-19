package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;

public class ProductException extends RuntimeException{

    ErrorCode errorCode;
    public ProductException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
