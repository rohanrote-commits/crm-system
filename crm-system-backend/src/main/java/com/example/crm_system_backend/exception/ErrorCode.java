package com.example.crm_system_backend.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Getter
public enum ErrorCode {
    INVALID_ADDRESS("400","Invalid Address", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("404","User Not Found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND("401","Account Not Found", HttpStatus.NOT_FOUND),
    WRONG_CREDENTIALS("401","Wrong Credentials", HttpStatus.UNAUTHORIZED),
    //ACCOUNT_ALREADY_IN_USE("409","Account Already In Use", HttpStatus.CONFLICT),
    ACCOUNT_ALREADY_EXISTS("409","Account Already Exists", HttpStatus.CONFLICT),
    MAXIMUM_USERACCOUNT_LIMIT_REACHED("409","Maximum UserAccount Limit Reached", HttpStatus.CONFLICT),

    AMOUNT_MUST_BE_POSITIVE("400","Amount must be positive", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("500","Insufficient Balance", HttpStatus.INTERNAL_SERVER_ERROR),
    ANOTHER_THREAD_EXECTING("410","Another Thread Executing", HttpStatus.GONE),;

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }


}
