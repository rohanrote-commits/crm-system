package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.service.serviceImpl.ErrorRecordService;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ErrorRecordHandler {


    private static final Logger log = LoggerFactory.getLogger(ErrorRecordHandler.class);
    private final ErrorRecordService errorRecordService;
    private final UploadHistoryService uploadHistoryService;
    private final LeadService leadService;
    private final ModelMapper modelMapper;


    public ErrorRecordHandler(ErrorRecordService errorRecordService, UploadHistoryService uploadHistoryService, LeadService leadService, ModelMapper modelMapper){
        this.errorRecordService = errorRecordService;
        this.uploadHistoryService = uploadHistoryService;
        this.leadService = leadService;
        this.modelMapper = modelMapper;
    }



    public ErrorRecord saveErrorRecord(ErrorRecord errorRecord){
        log.info("Enter: ErrorRecordHandler.saveErrorRecord");
      return  errorRecordService.saveErrorRecord(errorRecord);
    }

    public ErrorRecord findErrorRecordById(String id){
        log.info("Enter:ErrorRecordHandler.findErrorRecordById");
        return errorRecordService.findErrorRecordById(id).orElseThrow(
                ()-> {
                    log.error("Exit : ErrorRecordHandler.findErrorRecordById");
                   return new ExcelException(ErrorCode.NO_ERROR_RECORDS);
                }
        );
    }

    public ErrorRecord findErrorRecordByUploadHistoryId(String uploadHistoryId){
        log.info("Enter:ErrorRecordHandler.findErrorRecordByUploadHistoryId");
        return errorRecordService.findErrorRecordByUploadHistoryId(uploadHistoryId).orElseThrow(
                ()-> {
                    log.error("Exit : ErrorRecordHandler.findErrorRecordByUploadHistoryId");
                   return new ExcelException(ErrorCode.NO_ERROR_RECORDS);
                }
        );
    }

    @Transactional
    public LeadDto updateErrorRecord(String oldEmail,String uploadHistoryId, LeadDto leadDto){
    log.info("Enter: ErrorRecordHandler.updateErrorRecord");
        //delete error record from mongo error records
       ErrorRecord errorRecord =  errorRecordService.findErrorRecordByUploadHistoryId(uploadHistoryId).orElseThrow(
                ()-> {
                    log.error("Exit : ErrorRecordHandler.updateErrorRecord");
                    return new ExcelException(ErrorCode.NO_ERROR_RECORDS);
                }
        );
       errorRecord.getErrorsList().removeIf(lead1 -> oldEmail.equalsIgnoreCase(lead1.getEmail()));
       ErrorRecord savedRecord = errorRecordService.saveErrorRecord(errorRecord);

       //if error record list is empty then delete this record from db
       if(savedRecord.getErrorsList().isEmpty()){
            errorRecordService.deleteErrorRecordById(savedRecord.getId());
        }

       //update the correct lead in leads table with history table
        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
        uploadHistory.setValidRecords(uploadHistory.getValidRecords() + 1);
        uploadHistory.setInvalidRecords(uploadHistory.getInvalidRecords() - 1);
        Lead savedLead = leadService.save(leadDto);
        return modelMapper.map(savedLead, LeadDto.class);
    }

    public void deleteErrorRecordByEmail(String oldEmail,String uploadHistoryId) {
        log.info("Enter: ErrorRecordHandler.deleteErrorRecordByEmail");
        //delete error record from mongo error records
        ErrorRecord errorRecord =  errorRecordService.findErrorRecordByUploadHistoryId(uploadHistoryId).orElseThrow(
                ()-> {
                    log.error("Exit : ErrorRecordHandler.deleteErrorRecordByEmail -> Error Record not found");
                   return new ExcelException(ErrorCode.NO_ERROR_RECORDS);
                }
        );
        errorRecord.getErrorsList().removeIf(lead1 -> oldEmail.equalsIgnoreCase(lead1.getEmail()));
        ErrorRecord savedRecord = errorRecordService.saveErrorRecord(errorRecord);
        //If error record is empty then delete its ref from db
        if(savedRecord.getErrorsList().isEmpty()){
            errorRecordService.deleteErrorRecordById(savedRecord.getId());
        }
        log.info("Exit : ErrorRecordHandler.deleteErrorRecordByEmail");
    }
}
