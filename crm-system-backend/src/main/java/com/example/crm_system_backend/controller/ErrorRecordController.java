package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.annotations.RoleRequired;
import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.InvalidUserError;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/crm/error")
@AllArgsConstructor
public class ErrorRecordController {



    private ErrorRecordHandler errorRecordHandler;


    /**
     * Retrieves a list of lead error records associated with a specific upload history ID.
     * @author Akshay Jadhav
     * @param uploadHistoryId the ID of the upload history for which lead error records are to be retrieved
     * @return a ResponseEntity containing a list of InvalidLeadError objects and an HTTP status of OK
     */
    @GetMapping("/records/{uploadHistoryId}")
    public ResponseEntity< List<InvalidLeadError>> findErrorRecordByUploadHistoryId(@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.findErrorRecordByUploadHistoryId");
        List<InvalidLeadError> errorRecordList = errorRecordHandler.findErrorRecordByUploadHistoryId(uploadHistoryId);
      log.info("Exit: ErrorRecordController.findErrorRecordByUploadHistoryId");
      return new  ResponseEntity<>(errorRecordList, HttpStatus.OK);
    }

    /**
     * Retrieves a list of user error records associated with a specific upload history ID.
     *
     * @param uploadHistoryId the ID of the upload history for which user error records are to be retrieved
     * @return a ResponseEntity containing a list of InvalidUserError objects and an HTTP status of OK
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @GetMapping("/records/user/{uploadHistoryId}")
    public ResponseEntity< List<InvalidUserError>> findUserErrorRecordByUploadHistoryId(@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.findErrorRecordByUploadHistoryId");
        List<InvalidUserError> errorRecordList = errorRecordHandler.findUserErrorRecordByUploadHistoryId(uploadHistoryId);
        log.info("Exit: ErrorRecordController.findErrorRecordByUploadHistoryId");
        return new  ResponseEntity<>(errorRecordList, HttpStatus.OK);
    }


    /**
     * Updates a lead error record associated with a given row number and upload history ID.
     *
     * @author Akshay
     * @param rowNumber the row number of the error record to be updated
     * @param uploadHistoryId the ID of the upload history containing the error record
     * @param errorRecord the LeadDto object containing the updated error record information
     * @return a ResponseEntity containing the updated LeadDto object and an HTTP status of OK
     */
    @PutMapping("/lead/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<LeadDto> updateErrorRecord(@PathVariable int rowNumber,@PathVariable String uploadHistoryId ,@RequestBody LeadDto errorRecord){
        log.info("Enter: ErrorRecordController.updateErrorRecord");
       LeadDto leadDto = errorRecordHandler.updateErrorRecord(rowNumber,uploadHistoryId,errorRecord);
       log.info("Exit: ErrorRecordController.updateErrorRecord");
       return new  ResponseEntity<>(leadDto, HttpStatus.OK);
    }

    /**
     * Updates a user error record associated with a given row number and upload history ID.
     *
     * @author Akshay
     * @param rowNumber the row number of the error record to be updated
     * @param uploadHistoryId the ID of the upload history containing the error record
     * @param errorRecord the UserDTO object containing the updated error record information
     * @param request the HttpServletRequest object used to retrieve additional metadata, such as the registeredBy ID
     * @return a ResponseEntity containing the updated UserDTO object and an HTTP status of OK
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @PutMapping("/user/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<UserDTO> updateErrorRecord(@PathVariable int rowNumber, @PathVariable String uploadHistoryId , @RequestBody UserDTO errorRecord, HttpServletRequest request){
        Object registeredById = request.getAttribute("userId");
        if (registeredById != null) {
            errorRecord.setRegisteredBy((Long) registeredById);
        }
        UserDTO userDTO = errorRecordHandler.updateUserErrorRecord(rowNumber,uploadHistoryId,errorRecord);
        return new  ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    /**
     * Deletes an error record by the specified row number and upload history ID.
     *
     * @author Akshay
     * @param rowNumber the row number of the error record to be deleted
     * @param uploadHistoryId the ID of the upload history containing the error record to be deleted
     * @return a ResponseEntity with an HTTP status of OK upon successful deletion
     */
    @DeleteMapping("/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<?> deleteErrorRecordByEmail(@PathVariable int rowNumber,@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.deleteErrorRecordByEmail");
        errorRecordHandler.deleteErrorRecordByEmail(rowNumber,uploadHistoryId);
        log.info("Exit: ErrorRecordController.deleteErrorRecordByEmail");
        return new  ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Deletes a user error record by the specified row number and email associated with the upload history.
     *
     * @param rowNumber the row number of the error record to be deleted
     * @param uploadHistoryEmail the email associated with the upload history containing the error record
     * @return a ResponseEntity with an HTTP status of OK upon successful deletion
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @DeleteMapping("user/{rowNumber}/{uploadHistoryEmail}")
    public ResponseEntity<?> deleteUserErrorRecordByEmail(@PathVariable int rowNumber,@PathVariable String uploadHistoryEmail){
        errorRecordHandler.deleteUserErrorRecordByEmail(rowNumber,uploadHistoryEmail);
        return new  ResponseEntity<>(HttpStatus.OK);
    }

}
