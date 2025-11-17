package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
import com.example.crm_system_backend.service.serviceImpl.ErrorRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/crm/error/")
public class ErrorRecordController {


    @Autowired
    private ErrorRecordHandler errorRecordHandler;


    @GetMapping("/records/{uploadHistoryId}")
    public ResponseEntity<ErrorRecord> findErrorRecordByUploadHistoryId(@PathVariable String uploadHistoryId){
        log.info("Enter: ErrorRecordController.findErrorRecordByUploadHistoryId");
      ErrorRecord errorRecordList = errorRecordHandler.findErrorRecordByUploadHistoryId(uploadHistoryId);
      log.info("Exit: ErrorRecordController.findErrorRecordByUploadHistoryId");
      return new  ResponseEntity<>(errorRecordList, HttpStatus.OK);
    }

    @PutMapping("/{oldEmail}/{uploadHistoryId}")
    public ResponseEntity<LeadDto> updateErrorRecord(@PathVariable String oldEmail,@PathVariable String uploadHistoryId ,@RequestBody LeadDto errorRecord){
        log.info("Enter: ErrorRecordController.updateErrorRecord");
       LeadDto leadDto = errorRecordHandler.updateErrorRecord(oldEmail,uploadHistoryId,errorRecord);
       log.info("Exit: ErrorRecordController.updateErrorRecord");
       return new  ResponseEntity<>(leadDto, HttpStatus.OK);
    }
    @DeleteMapping("{email}/{uploadHistoryEmail}")
    public ResponseEntity<?> deleteErrorRecordByEmail(@PathVariable String email,@PathVariable String uploadHistoryEmail){
        log.info("Enter: ErrorRecordController.deleteErrorRecordByEmail");
        errorRecordHandler.deleteErrorRecordByEmail(email,uploadHistoryEmail);
        log.info("Exit: ErrorRecordController.deleteErrorRecordByEmail");
        return new  ResponseEntity<>(HttpStatus.OK);
    }
}
