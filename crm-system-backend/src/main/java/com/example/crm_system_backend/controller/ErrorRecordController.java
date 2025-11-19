package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
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


    @GetMapping("/records/{uploadHistoryId}")
    public ResponseEntity< List<InvalidLeadError>> findErrorRecordByUploadHistoryId(@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.findErrorRecordByUploadHistoryId");
        List<InvalidLeadError> errorRecordList = errorRecordHandler.findErrorRecordByUploadHistoryId(uploadHistoryId);
      log.info("Exit: ErrorRecordController.findErrorRecordByUploadHistoryId");
      return new  ResponseEntity<>(errorRecordList, HttpStatus.OK);
    }

    @PutMapping("/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<LeadDto> updateErrorRecord(@PathVariable int rowNumber,@PathVariable String uploadHistoryId ,@RequestBody LeadDto errorRecord){
        log.info("Enter: ErrorRecordController.updateErrorRecord");
       LeadDto leadDto = errorRecordHandler.updateErrorRecord(rowNumber,uploadHistoryId,errorRecord);
       log.info("Exit: ErrorRecordController.updateErrorRecord");
       return new  ResponseEntity<>(leadDto, HttpStatus.OK);
    }
    @PutMapping("/user/{oldEmail}/{uploadHistoryId}")
    public ResponseEntity<UserDTO> updateErrorRecord(@PathVariable String oldEmail, @PathVariable String uploadHistoryId , @RequestBody UserDTO errorRecord){
        UserDTO userDTO = errorRecordHandler.updateUserErrorRecord(oldEmail,uploadHistoryId,errorRecord);
        return new  ResponseEntity<>(userDTO, HttpStatus.OK);
    }
    @DeleteMapping("/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<?> deleteErrorRecordByEmail(@PathVariable int rowNumber,@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.deleteErrorRecordByEmail");
        errorRecordHandler.deleteErrorRecordByEmail(rowNumber,uploadHistoryId);
        log.info("Exit: ErrorRecordController.deleteErrorRecordByEmail");
        return new  ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("user/{email}/{uploadHistoryEmail}")
    public ResponseEntity<?> deleteUserErrorRecordByEmail(@PathVariable String email,@PathVariable String uploadHistoryEmail){
        errorRecordHandler.deleteUserErrorRecordByEmail(email,uploadHistoryEmail);
        return new  ResponseEntity<>(HttpStatus.OK);
    }

}
