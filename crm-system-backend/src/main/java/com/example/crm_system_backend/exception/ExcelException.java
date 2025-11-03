package com.example.crm_system_backend.exception;

public class ExcelException extends RuntimeException{

  public ExcelException(ErrorCode code){
        super(code.getMessage());
    }
}
