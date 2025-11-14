package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.service.serviceImpl.ErrorRecordService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ErrorRecordHandler {


    private final ErrorRecordService errorRecordService;


    public ErrorRecordHandler(ErrorRecordService errorRecordService){
        this.errorRecordService = errorRecordService;
    }



    public ErrorRecord saveErrorRecord(List<Lead> errorList, UploadHistory uploadHistory){
        ErrorRecord errorRecord = new ErrorRecord();
        errorRecord.setErrorsList(errorList);
        errorRecord.setFileName(uploadHistory.getFileName());
        errorRecord.setUploadHistoryId(uploadHistory.getId());
        errorRecord.setUplodedBy(uploadHistory.getUploadedBy());
      return   errorRecordService.saveErrorRecord(errorRecord);
    }

    public ErrorRecord findErrorRecordById(String id){
        return errorRecordService.findErrorRecordById(id).orElseThrow(
                ()-> new ExcelException(ErrorCode.NO_ERROR_RECORDS)
        );
    }

    public ErrorRecord findErrorRecordByUploadHistoryId(String uploadHistoryId){
        return errorRecordService.findErrorRecordByUploadHistoryId(uploadHistoryId).orElseThrow(
                ()-> new ExcelException(ErrorCode.NO_ERROR_RECORDS)
        );
    }
}
