package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.annotations.RoleRequired;
import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.InvalidUserError;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Error Records", description = "APIs for managing error records for leads and users")
public class ErrorRecordController {



    private ErrorRecordHandler errorRecordHandler;


    @Operation(summary = "Find lead error records by upload history ID",
            description = "Retrieves all invalid lead error records associated with the given upload history ID")
    @Parameter(name = "uploadHistoryId", description = "The ID of the upload history to search for", required = true)
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
    @Operation(summary = "Find user error records by upload history ID",
            description = "Retrieves all invalid user error records associated with the given upload history ID")
    @Parameter(name = "uploadHistoryId", description = "The ID of the upload history to search for", required = true)
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @GetMapping("/records/user/{uploadHistoryId}")
    public ResponseEntity< List<InvalidUserError>> findUserErrorRecordByUploadHistoryId(@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.findErrorRecordByUploadHistoryId");
        List<InvalidUserError> errorRecordList = errorRecordHandler.findUserErrorRecordByUploadHistoryId(uploadHistoryId);
        log.info("Exit: ErrorRecordController.findErrorRecordByUploadHistoryId");
        return new  ResponseEntity<>(errorRecordList, HttpStatus.OK);
    }


    @Operation(summary = "Update lead error record",
            description = "Updates a lead error record for the specified row number and upload history ID")
    @Parameter(name = "rowNumber", description = "The row number of the error record", required = true)
    @Parameter(name = "uploadHistoryId", description = "The ID of the upload history", required = true)
    @PutMapping("/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<LeadDto> updateErrorRecord(@PathVariable int rowNumber,@PathVariable String uploadHistoryId ,@RequestBody LeadDto errorRecord){
        log.info("Enter: ErrorRecordController.updateErrorRecord");
       LeadDto leadDto = errorRecordHandler.updateErrorRecord(rowNumber,uploadHistoryId,errorRecord);
       log.info("Exit: ErrorRecordController.updateErrorRecord");
       return new  ResponseEntity<>(leadDto, HttpStatus.OK);
    }

    /**
     * Updates an error record for a specific user associated with a given row number and upload history ID.
     * The method also updates the record with the "registered by" user ID retrieved from the HTTP request attributes.
     *
     */
    @Operation(summary = "Update user error record",
            description = "Updates a user error record for the specified row number and upload history ID")
    @Parameter(name = "rowNumber", description = "The row number of the error record", required = true)
    @Parameter(name = "uploadHistoryId", description = "The ID of the upload history", required = true)
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


    @Operation(summary = "Delete lead error record",
            description = "Deletes a lead error record for the specified row number and upload history ID")
    @Parameter(name = "rowNumber", description = "The row number of the error record", required = true)
    @Parameter(name = "uploadHistoryId", description = "The ID of the upload history", required = true)
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
    @Operation(summary = "Delete user error record",
            description = "Deletes a user error record for the specified row number and upload history email")
    @Parameter(name = "rowNumber", description = "The row number of the error record", required = true)
    @Parameter(name = "uploadHistoryEmail", description = "The email associated with the upload history", required = true)
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @DeleteMapping("user/{rowNumber}/{uploadHistoryEmail}")
    public ResponseEntity<?> deleteUserErrorRecordByEmail(@PathVariable int rowNumber,@PathVariable String uploadHistoryEmail){
        errorRecordHandler.deleteUserErrorRecordByEmail(rowNumber,uploadHistoryEmail);
        return new  ResponseEntity<>(HttpStatus.OK);
    }

}
