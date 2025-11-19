package com.example.crm_system_backend.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_ADDRESS("400", "Invalid Address", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("404", "User Not Found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND("401", "Account Not Found", HttpStatus.NOT_FOUND),
    WRONG_CREDENTIALS("401", "Wrong Credentials", HttpStatus.UNAUTHORIZED),
    ACCOUNT_ALREADY_EXISTS("409", "Account Already Exists", HttpStatus.CONFLICT),
    MAXIMUM_USERACCOUNT_LIMIT_REACHED("409", "Maximum UserAccount Limit Reached", HttpStatus.CONFLICT),
    AMOUNT_MUST_BE_POSITIVE("400", "Amount must be positive", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("500", "Insufficient Balance", HttpStatus.INTERNAL_SERVER_ERROR),
    ANOTHER_THREAD_EXECTING("410", "Another Thread Executing", HttpStatus.GONE),
    LEAD_NOT_FOUND("LNF", "Lead Not Found", HttpStatus.NOT_FOUND),
    LEAD_ALREADY_EXISTS("LAE", "Lead Already Exists", HttpStatus.CONFLICT),
    USER_ALREADY_EXISTS("LAE", "User Already Exists", HttpStatus.CONFLICT),
    ERROR_IN_FILE_DOWNLOAD("EDF", "Error in File Download", HttpStatus.INTERNAL_SERVER_ERROR),
    WRONG_HEADERS("WH", "Wrong Headers", HttpStatus.NOT_ACCEPTABLE),
    PASSWORD_SHOULD_NOT_BE_SAME("PSNS", "Password Should Not Be Same", HttpStatus.NOT_ACCEPTABLE),
    ANOTHER_SESSION_ACTIVE_FOR_USER("ASAU", "Another Session Active For User, Login Again", HttpStatus.CONFLICT),
    USER_NOT_ALLOWED_TO_ACCESS("UNA", "User Not Allowed To Access", HttpStatus.UNAUTHORIZED),

    FILE_PROCESSING_EXCEPTION("FPE", "File Processing Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND_EXCEPTION("FNF", "File Not Found", HttpStatus.NOT_FOUND),
    FILE_HISTORY_NOT_FOUND("FHN", "File History Not Found", HttpStatus.NOT_FOUND),
    FILE_PROCESSING_FAILED("FPF", "File Processing Failed", HttpStatus.NOT_ACCEPTABLE),
    NO_ERROR_RECORDS("NER", "No Error Records", HttpStatus.NOT_FOUND),
    USER_NOT_PRESENT_WITH_EMAIL("UNPE", "User Not Present With Email", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("EAE", "Email Already Exists", HttpStatus.CONFLICT),
    MOBILE_NUMBER_ALREADY_EXISTS("MNAE", "Mobile Number Already Exists", HttpStatus.CONFLICT),
    USER_DATA_NOT_UPDATABLE("UDNU", "User Data Not Updatable", HttpStatus.NOT_ACCEPTABLE),
    SESSION_EXPIRED("SE", "Session Expired", HttpStatus.UNAUTHORIZED),
    ANOTHER_SESSION_ACTIVE("ASA", "Another Session Active", HttpStatus.CONFLICT),
    DATA_INSERTED_PARTIALLY("DIP", "Data Inserted Partially", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_NOT_INSERTED("DNI", "Data Not Inserted", HttpStatus.INTERNAL_SERVER_ERROR),
    ERROR_IN_FILE_PROCESSING("EFP", "Error in File Processing", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_FORMAT("IF", "Invalid File Format", HttpStatus.NOT_ACCEPTABLE),

   INVALID_USER_NOT_ACTIVE("INUA", "Invalid User Not Found", HttpStatus.NOT_ACCEPTABLE),
    PRODUCT_NOT_FOUND("PNF","PRODUCT NOT EXIST",HttpStatus.NOT_FOUND),
    INVALID_LEAD_NOT_FOUND("ILNF", "Invalid Lead Not Found" , HttpStatus.NOT_FOUND );

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }


}
