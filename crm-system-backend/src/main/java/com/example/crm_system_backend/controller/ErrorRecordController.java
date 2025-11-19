package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.InvalidUserError;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.crm_system_backend.dto.UserDTO;

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

    @GetMapping("/records/user/{uploadHistoryId}")
    public ResponseEntity< List<InvalidUserError>> findUserErrorRecordByUploadHistoryId(@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.findErrorRecordByUploadHistoryId");
        List<InvalidUserError> errorRecordList = errorRecordHandler.findUserErrorRecordByUploadHistoryId(uploadHistoryId);
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
    @PutMapping("/user/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<UserDTO> updateErrorRecord(@PathVariable int rowNumber, @PathVariable String uploadHistoryId , @RequestBody UserDTO errorRecord, HttpServletRequest request){
        Object registeredById = request.getAttribute("userId");
        if (registeredById != null) {
            errorRecord.setRegisteredBy((Long) registeredById);
        }
        UserDTO userDTO = errorRecordHandler.updateUserErrorRecord(rowNumber,uploadHistoryId,errorRecord);
        return new  ResponseEntity<>(userDTO, HttpStatus.OK);
    }
    @DeleteMapping("/{rowNumber}/{uploadHistoryId}")
    public ResponseEntity<?> deleteErrorRecordByEmail(@PathVariable int rowNumber,@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.deleteErrorRecordByEmail");
        errorRecordHandler.deleteErrorRecordByEmail(rowNumber,uploadHistoryId);
        log.info("Exit: ErrorRecordController.deleteErrorRecordByEmail");
        return new  ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("user/{rowNumber}/{uploadHistoryEmail}")
    public ResponseEntity<?> deleteUserErrorRecordByEmail(@PathVariable int rowNumber,@PathVariable String uploadHistoryEmail){
        errorRecordHandler.deleteUserErrorRecordByEmail(rowNumber,uploadHistoryEmail);
        return new  ResponseEntity<>(HttpStatus.OK);
    }

}
