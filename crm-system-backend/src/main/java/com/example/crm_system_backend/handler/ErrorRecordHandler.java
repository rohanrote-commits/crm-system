package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.ErrorRecordService;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
<<<<<<< Updated upstream
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
=======
import org.springframework.beans.BeanUtils;
>>>>>>> Stashed changes
import org.springframework.stereotype.Component;


@Component
public class ErrorRecordHandler {


    private static final Logger log = LoggerFactory.getLogger(ErrorRecordHandler.class);
    private final ErrorRecordService errorRecordService;
    private final UploadHistoryService uploadHistoryService;
    private final LeadService leadService;
    private final ModelMapper modelMapper;
    private final UserService userService;


    public ErrorRecordHandler(ErrorRecordService errorRecordService, UploadHistoryService uploadHistoryService, LeadService leadService, ModelMapper modelMapper, UserService userService){
        this.errorRecordService = errorRecordService;
        this.uploadHistoryService = uploadHistoryService;
        this.leadService = leadService;
        this.modelMapper = modelMapper;
        this.userService = userService;
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

    @Transactional
    public UserDTO updateUserErrorRecord(String oldEmail, String uploadHistoryId, UserDTO userDTO){

        // 1. Fetch the error record
        ErrorRecord errorRecord = errorRecordService.findErrorRecordByUploadHistoryId(uploadHistoryId)
                .orElseThrow(() -> new ExcelException(ErrorCode.NO_ERROR_RECORDS));

        // 2. Fetch upload history and parent user
        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
        User parentUser = userService.getUserByEmail(errorRecord.getUplodedBy())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        // 3. Convert UserDTO → User (the corrected user)
        User user = modelMapper.map(userDTO, User.class);
        user.setRegisteredBy(parentUser.getId());

        // 4. Try saving the corrected user FIRST
        User savedUser = userService.registerUser(user);

        // 5. Update upload history counts
        uploadHistory.setValidRecords(uploadHistory.getValidRecords() + 1);
        uploadHistory.setInvalidRecords(uploadHistory.getInvalidRecords() - 1);
        uploadHistoryService.save(uploadHistory);

        // 6. ONLY AFTER SUCCESS → remove user from error record
        errorRecord.getErrorUserList()
                .removeIf(u -> oldEmail.equalsIgnoreCase(u.getEmail()));

        ErrorRecord savedRecord = errorRecordService.saveErrorRecord(errorRecord);

        // 7. If the error list becomes empty → delete the entire record
        if (savedRecord.getErrorUserList().isEmpty()) {
            errorRecordService.deleteErrorRecordById(savedRecord.getId());
        }

        return modelMapper.map(savedUser, UserDTO.class);
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

    public void deleteUserErrorRecordByEmail(String oldEmail,String uploadHistoryId) {
        //delete error record from mongo error records
        ErrorRecord errorRecord =  errorRecordService.findErrorRecordByUploadHistoryId(uploadHistoryId).orElseThrow(
                ()-> new ExcelException(ErrorCode.NO_ERROR_RECORDS)
        );
        errorRecord.getErrorUserList().removeIf(user -> oldEmail.equalsIgnoreCase(user.getEmail()));
        ErrorRecord savedRecord = errorRecordService.saveErrorRecord(errorRecord);
    }
}
