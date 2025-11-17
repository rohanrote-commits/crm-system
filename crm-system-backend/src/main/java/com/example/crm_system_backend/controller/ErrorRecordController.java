package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
import com.example.crm_system_backend.service.serviceImpl.ErrorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/error/")
public class ErrorRecordController {


    @Autowired
    private ErrorRecordHandler errorRecordHandler;


    @GetMapping("/records/{uploadHistoryId}")
    public ResponseEntity<ErrorRecord> findErrorRecordByUploadHistoryId(@PathVariable String uploadHistoryId){
      ErrorRecord errorRecordList = errorRecordHandler.findErrorRecordByUploadHistoryId(uploadHistoryId);
      return new  ResponseEntity<>(errorRecordList, HttpStatus.OK);
    }

    @PutMapping("/{oldEmail}/{uploadHistoryId}")
    public ResponseEntity<LeadDto> updateErrorRecord(@PathVariable String oldEmail,@PathVariable String uploadHistoryId ,@RequestBody LeadDto errorRecord){
       LeadDto leadDto = errorRecordHandler.updateErrorRecord(oldEmail,uploadHistoryId,errorRecord);
       return new  ResponseEntity<>(leadDto, HttpStatus.OK);
    }
    @DeleteMapping("{email}/{uploadHistoryEmail}")
    public ResponseEntity<?> deleteErrorRecordByEmail(@PathVariable String email,@PathVariable String uploadHistoryEmail){
        errorRecordHandler.deleteErrorRecordByEmail(email,uploadHistoryEmail);
        return new  ResponseEntity<>(HttpStatus.OK);
    }
}
