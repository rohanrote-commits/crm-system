package com.example.crm_system_backend.handler;


import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.InvalidUserError;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorRecordException;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.UploadHistoryException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.ErrorRecordService;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class ErrorRecordHandler {


    private static final Logger log = LoggerFactory.getLogger(ErrorRecordHandler.class);
    private final ErrorRecordService errorRecordService;
    private final UploadHistoryService uploadHistoryService;
    private final LeadService leadService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final IUserRepo userRepo;
    private final ObjectMapper objectMapper;



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
    public List<InvalidUserError> findUserErrorRecordByUploadHistoryId(String uploadHistoryId){
        log.info("Enter:ErrorRecordHandler.findErrorRecordByUploadHistoryId");
        UploadHistory history = uploadHistoryService.findById(uploadHistoryId);
        if (history.getErrorRecord() == null) {
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<InvalidUserError> errorList =
                    mapper.readValue(history.getErrorRecord(), new TypeReference<List<InvalidUserError>>() {});

            return errorList;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel from JSON", e);
        }
    }


    //changes are here
    public List<InvalidLeadError> findErrorRecordByUploadHistoryId(String uploadHistoryId){
        log.info("Enter:ErrorRecordHandler.findErrorRecordByUploadHistoryId");
        UploadHistory history = uploadHistoryService.findById(uploadHistoryId);
        if (history.getErrorRecord() == null) {
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }

        try {
            List<InvalidLeadError> errorList =
                    objectMapper.readValue(history.getErrorRecord(), new TypeReference<List<InvalidLeadError>>() {});

            return errorList;

        } catch (Exception e) {
            log.error(e.toString());
            log.error("Exception : ErrorRecordHandler.findErrorRecordByUploadHistoryId");
            throw new ErrorRecordException(ErrorCode.NO_ERROR_RECORDS);
        }
    }

    @Transactional
    public LeadDto updateErrorRecord(int rowNumber, String uploadHistoryId, LeadDto leadDto) {
        log.info("Enter: ErrorRecordHandler.updateErrorRecord");
        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);

        if (uploadHistory.getErrorRecord() == null) {
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }

        try {
            // 1 Read JSON → List<InvalidLeadError>
            List<InvalidLeadError> errorList =
                    objectMapper.readValue(
                            uploadHistory.getErrorRecord(),
                            new TypeReference<List<InvalidLeadError>>() {}
                    );
            // 2 Find the invalid lead by rowNumber
            InvalidLeadError toFix = errorList.stream()
                    .filter(e -> e.getRowNumber() == rowNumber)
                    .findFirst()
                    .orElseThrow(() -> new ErrorRecordException(ErrorCode.INVALID_LEAD_NOT_FOUND));

            // 3 Remove the resolved error record
            errorList.remove(toFix);
            // 4 Save updated JSON back to DB
            uploadHistory.setErrorRecord(objectMapper.writeValueAsString(errorList));
            //update error record number
            uploadHistory.setInvalidRecords(uploadHistory.getInvalidRecords()-1);
            uploadHistory.setValidRecords(uploadHistory.getValidRecords()+1);
            uploadHistory.setUpdatedAt(LocalDateTime.now());
           UploadHistory savedUploadHistory1 =  uploadHistoryService.save(uploadHistory);
           //if no error record then status is success
           if (savedUploadHistory1.getErrorRecord() == null || savedUploadHistory1.getErrorRecord().isEmpty()) {
               uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
               uploadHistoryService.save(uploadHistory);
           }

            // 5 Save corrected lead as valid lead
            Lead savedLead = leadService.save(leadDto);
            return modelMapper.map(savedLead, LeadDto.class);

        } catch (Exception e) {
            log.error("Exception in updateErrorRecord", e);
            throw new ErrorRecordException(ErrorCode.NO_ERROR_RECORDS);
        }
    }

    @Transactional
    public UserDTO updateUserErrorRecord(int rowNumber, String uploadHistoryId, UserDTO userDTO){
        log.info("Enter: ErrorRecordHandler.updateErrorRecord");
        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);

        if (uploadHistory.getErrorRecord() == null) {
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }

        try {
            // 1 Read JSON → List<InvalidUserError>
            List<InvalidUserError> errorList =
                    objectMapper.readValue(
                            uploadHistory.getErrorRecord(),
                            new TypeReference<List<InvalidUserError>>() {}
                    );
            // 2 Find the invalid User by rowNumber
            InvalidUserError toFix = errorList.stream()
                    .filter(e -> e.getRowNumber() == rowNumber)
                    .findFirst()
                    .orElseThrow(() -> new ErrorRecordException(ErrorCode.INVALID_USER_NOT_ACTIVE));

            // 3 Remove the resolved error record
            errorList.remove(toFix);
            // 4 Save updated JSON back to DB
            uploadHistory.setErrorRecord(objectMapper.writeValueAsString(errorList));
            //update error record number
            uploadHistory.setInvalidRecords(uploadHistory.getInvalidRecords()-1);
            uploadHistory.setValidRecords(uploadHistory.getValidRecords()+1);
            uploadHistory.setUpdatedAt(LocalDateTime.now());
            UploadHistory savedUploadHistory1 =  uploadHistoryService.save(uploadHistory);
            //if no error record then status is success
            if(hasNoErrors(savedUploadHistory1)){
                savedUploadHistory1.setUploadStatus(UploadStatus.SUCCESS);
            }
            uploadHistoryService.save(savedUploadHistory1);

            // 5 Save and return corrected user
            if(userRepo.existsByEmail(userDTO.getEmail())){
                throw new UserException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            if(userRepo.existsByMobileNumber(userDTO.getMobileNumber())){
                throw new UserException(ErrorCode.MOBILE_NUMBER_ALREADY_EXISTS);
            }
            User user = new User();
            BeanUtils.copyProperties(userDTO,user);
            User savedUser = userRepo.save(user);



            return modelMapper.map(savedUser, UserDTO.class);

        } catch (Exception e) {
            log.error("Exception in updateErrorRecord", e);
            throw new ErrorRecordException(ErrorCode.NO_ERROR_RECORDS);
        }
    }


    public void deleteErrorRecordByEmail(int rowNumber,String uploadHistoryId) {
        log.info("Enter: ErrorRecordHandler.deleteErrorRecordByEmail");

        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
        if (uploadHistory.getErrorRecord() == null) {
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }

        try {
            // 1 Read JSON → List<InvalidLeadError>
            List<InvalidLeadError> errorList =
                    objectMapper.readValue(
                            uploadHistory.getErrorRecord(),
                            new TypeReference<List<InvalidLeadError>>() {
                            }
                    );
            // 2 Find the invalid lead by rowNumber
            InvalidLeadError toFix = errorList.stream()
                    .filter(e -> e.getRowNumber() == rowNumber)
                    .findFirst()
                    .orElseThrow(() ->{
                        log.error("Exception : ErrorRecordHandler.deleteErrorRecordByEmail -->InvalidLeadNotFound");
                       return new ErrorRecordException(ErrorCode.INVALID_LEAD_NOT_FOUND);
                    });
            errorList.remove(toFix);
            // 4 Save updated JSON back to DB
            uploadHistory.setErrorRecord(objectMapper.writeValueAsString(errorList));
            //update error record number
            uploadHistory.setInvalidRecords(uploadHistory.getInvalidRecords()-1);
            uploadHistory.setValidRecords(uploadHistory.getValidRecords()+1);
            uploadHistory.setUpdatedAt(LocalDateTime.now());
            UploadHistory savedUploadHistory1 =  uploadHistoryService.save(uploadHistory);
            //if no error record then status is success
            if (savedUploadHistory1.getErrorRecord() == null || savedUploadHistory1.getErrorRecord().isEmpty()) {
                uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
                uploadHistoryService.save(uploadHistory);
            }
        } catch (Exception e) {
            log.error("Exception in updateErrorRecord", e);
            throw new ErrorRecordException(ErrorCode.NO_ERROR_RECORDS);
        }
        log.info("Exit : ErrorRecordHandler.deleteErrorRecordByEmail");
    }

    public void deleteUserErrorRecordByEmail(int rowNumber,String uploadHistoryId) {
        log.info("Enter: ErrorRecordHandler.deleteErrorRecordByEmail");

        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
        if (uploadHistory.getErrorRecord() == null) {
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }

        try {
            // 1 Read JSON → List<InvalidLeadError>
            List<InvalidUserError> errorList =
                    objectMapper.readValue(
                            uploadHistory.getErrorRecord(),
                            new TypeReference<List<InvalidUserError>>() {
                            }
                    );
            // 2 Find the invalid lead by rowNumber
            InvalidUserError toFix = errorList.stream()
                    .filter(e -> e.getRowNumber() == rowNumber)
                    .findFirst()
                    .orElseThrow(() ->{
                        log.error("Exception : ErrorRecordHandler.deleteErrorRecordByEmail -->InvalidLeadNotFound");
                        return new ErrorRecordException(ErrorCode.INVALID_USER_NOT_ACTIVE);
                    });
            errorList.remove(toFix);
            // 4 Save updated JSON back to DB
            uploadHistory.setErrorRecord(objectMapper.writeValueAsString(errorList));
            //update error record number
            uploadHistory.setInvalidRecords(uploadHistory.getInvalidRecords()-1);
            uploadHistory.setValidRecords(uploadHistory.getValidRecords()+1);
            uploadHistory.setUpdatedAt(LocalDateTime.now());
            UploadHistory savedUploadHistory1 =  uploadHistoryService.save(uploadHistory);
            //if no error record then status is success
           if(hasNoErrors(savedUploadHistory1)){
               savedUploadHistory1.setUploadStatus(UploadStatus.SUCCESS);
           }
            uploadHistoryService.save(savedUploadHistory1);
        } catch (Exception e) {
            log.error("Exception in updateErrorRecord", e);
            throw new ErrorRecordException(ErrorCode.NO_ERROR_RECORDS);
        }
        log.info("Exit : ErrorRecordHandler.deleteErrorRecordByEmail");
    }

    private boolean hasNoErrors(UploadHistory history) {
        try {
            String json = history.getErrorRecord();
            if (json == null) return true;
            // Normalize whitespace
            json = json.trim();
            if ("[]".equals(json)) return true;
            // Parse and check size
            List<Object> items = objectMapper.readValue(json, new TypeReference<List<Object>>() {});
            return items == null || items.isEmpty();
        } catch (Exception e) {
            log.warn("Failed to parse errorRecord; treating as not empty. Value: {}", history.getErrorRecord(), e);
            return false;
        }
    }




}
