package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
import com.example.crm_system_backend.service.serviceImpl.ErrorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
