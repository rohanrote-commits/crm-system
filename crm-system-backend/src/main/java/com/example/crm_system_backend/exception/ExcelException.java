package com.example.crm_system_backend.exception;

import com.example.crm_system_backend.constants.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
@Data
public class ExcelException extends RuntimeException {

    ErrorCode errorCode;
    public ExcelException(ErrorCode code) {
        super(code.getMessage());
        errorCode = code;
    }

}
