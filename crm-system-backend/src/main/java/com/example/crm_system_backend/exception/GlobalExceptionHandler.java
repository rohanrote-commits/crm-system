package com.example.crm_system_backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException userException) {
        ErrorResponse errorResponse = new ErrorResponse(
                userException.errorCode.getMessage(),
                userException.errorCode.getStatus()
        );

        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }


    @ExceptionHandler(ExcelProcessingError.class)
    public ResponseEntity<byte[]> handleExcelProcessingError(ExcelProcessingError exception) {

        return ResponseEntity.internalServerError().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE).body(exception.file);
    }

    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<ErrorResponse> handleFileDownloadException(FileDownloadException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getErrorCode().getMessage(),
                exception.getErrorCode().getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    /**
     * Handles the {@link LeadException} by constructing an appropriate error response
     * encapsulated in a {@link ResponseEntity}.
     *
     * @param exception the {@link LeadException} encountered during processing, containing
     *                  details such as the error code and error message representing
     *                  the specific error condition
     * @return a {@link ResponseEntity} containing the constructed {@link ErrorResponse}
     *         with the associated error message and HTTP status derived from the exception
     * @author Akshay Jadhav
     */
    @ExceptionHandler(LeadException.class)
    public ResponseEntity<ErrorResponse> handleLeadException(LeadException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                exception.errorCode.getMessage(),
                exception.errorCode.getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    /**
     * Handles exceptions of type {@link ExcelException} and returns an appropriate error response
     * encapsulated in a {@link ResponseEntity}.
     *
     * @param ex the {@link ExcelException} encountered during processing, containing
     *           details such as the error code and message to represent the specific error condition
     * @return a {@link ResponseEntity} containing the constructed {@link ErrorResponse} with
     *         the associated error message and HTTP status
     * @author Akshay Jadhav
     */
    @ExceptionHandler(ExcelException.class)
    public ResponseEntity<ErrorResponse> handleExcelException(ExcelException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.errorCode.getMessage(),
                ex.errorCode.getStatus()
        );

        return new ResponseEntity<>(response, response.getStatus());
    }
    /**
     * Handles exceptions of type {@link UploadHistoryException} and provides an appropriate error
     * response encapsulated in a {@link ResponseEntity}.
     *
     * @param exception the {@link UploadHistoryException} encountered during processing. It contains
     *                  details such as the error code and message representing the specific error condition.
     * @return a {@link ResponseEntity} containing the constructed {@link ErrorResponse} with the error
     *         message and associated HTTP status derived from the exception.
     * @author Akshay Jadhav
     */
    @ExceptionHandler(UploadHistoryException.class)
    public  ResponseEntity<ErrorResponse> handleUploadHistoryException(UploadHistoryException exception){
        ErrorResponse errorResponse = new ErrorResponse(
                exception.errorCode.getMessage(),
                exception.errorCode.getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    /**
     * Handles exceptions of type {@link ProductException} and returns an appropriate error response
     * encapsulated in a {@link ResponseEntity}.
     *
     * @param exception the {@link ProductException} encountered during processing, containing
     *                  details such as error code and error message
     * @return a {@link ResponseEntity} containing the constructed {@link ErrorResponse} with
     *         the associated error message and HTTP status
     */
    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> handleProductException(ProductException exception){
        ErrorResponse errorResponse = new ErrorResponse(
                exception.errorCode.getMessage(),
                exception.errorCode.getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    /**
     * Handles the {@link ErrorRecordException} and returns an appropriate error response encapsulated
     * in a {@link ResponseEntity}.
     *
     * @param exception the {@link ErrorRecordException} encountered during processing
     * @return a {@link ResponseEntity} containing the error message and HTTP status derived from the exception
     */
    @ExceptionHandler(ErrorRecordException.class)
    public ResponseEntity<ErrorResponse> handleErrorRecordException(ErrorRecordException exception){
        ErrorResponse errorResponse = new ErrorResponse(
                exception.errorCode.getMessage(),
                exception.errorCode.getStatus()
        );
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

}
