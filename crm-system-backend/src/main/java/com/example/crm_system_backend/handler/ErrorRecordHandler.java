package com.example.crm_system_backend.handler;


import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.InvalidUserError;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorRecordException;
import com.example.crm_system_backend.exception.UploadHistoryException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final UploadHistoryService uploadHistoryService;
    private final LeadService leadService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final IUserRepo userRepo;
    private final ObjectMapper objectMapper;



    public List<InvalidLeadError> findErrorRecordByUploadHistoryId(String uploadHistoryId){
        log.info("Enter:ErrorRecordHandler.findErrorRecordByUploadHistoryId");
        UploadHistory history = uploadHistoryService.findById(uploadHistoryId);
        if (history.getErrorRecord() == null) {
            log.error("Exception : ErrorRecordHandler.findErrorRecordByUploadHistoryId ---> for uploadHistoryId {}",uploadHistoryId);
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }

        try {
            List<InvalidLeadError> errorList =
                    objectMapper.readValue(history.getErrorRecord(), new TypeReference<List<InvalidLeadError>>() {});

            log.info("Exit: ErrorRecordHandler.findErrorRecordByUploadHistoryId");
            return errorList;

        } catch (Exception e) {
            log.error(e.toString());
            log.error("Exception : ErrorRecordHandler.findErrorRecordByUploadHistoryId");
            throw new ErrorRecordException(ErrorCode.NO_ERROR_RECORDS);
        }
    }

    /**
     * Retrieves a list of invalid user error records based on the given upload history ID.
     * This method fetches the associated upload history, extracts the error records in JSON format,
     * and converts them into a list of {@code InvalidUserError} objects.
     *
     * @param uploadHistoryId the unique identifier of the upload history containing error records
     * @return a list of {@code InvalidUserError} objects representing the invalid user errors
     *         associated with the provided upload history
     * @throws UploadHistoryException if the upload history does not contain error records
     * @throws RuntimeException if there is an issue while processing the error records
     */
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
            if(!errorList.isEmpty() ){
                uploadHistory.setErrorRecord(objectMapper.writeValueAsString(errorList));
            } else {
              uploadHistory.setErrorRecord(null);
            }
            // 4 Save updated JSON back to DB

            //update error record number
            uploadHistory.setInvalidRecords(uploadHistory.getInvalidRecords()-1);
            uploadHistory.setValidRecords(uploadHistory.getValidRecords()+1);
            uploadHistory.setUpdatedAt(LocalDateTime.now());
           UploadHistory savedUploadHistory1 =  uploadHistoryService.save(uploadHistory);
           //if no error record then status is success
           if (savedUploadHistory1.getErrorRecord() == null || savedUploadHistory1.getErrorRecord().isEmpty()) {
               savedUploadHistory1.setUploadStatus(UploadStatus.SUCCESS);
               uploadHistoryService.save(savedUploadHistory1);
           }

            // 5 Save corrected lead as valid lead
            Lead savedLead = leadService.save(leadDto);
            return modelMapper.map(savedLead, LeadDto.class);

        } catch (Exception e) {
            log.error("Exception in updateErrorRecord", e);
            throw new ErrorRecordException(ErrorCode.NO_ERROR_RECORDS);
        }
    }

    /**
     * Updates the error record for a user in the system by removing the error from the upload history
     * and saving the updated user data.
     *
     * @param rowNumber the row number of the invalid user record to be fixed
     * @param uploadHistoryId the identifier for the upload history containing error records
     * @param userDTO the updated user details to replace the invalid user record
     * @return the updated {@code UserDTO} object containing corrected user details
     * @throws UploadHistoryException if the upload history does not exist or contains no error records
     * @throws ErrorRecordException if the specified error record cannot be found or other issues occur during processing
     * @throws UserException if a user with the provided email or mobile number already exists
     */
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
                log.error("User with email {} already exists", userDTO.getEmail());
                throw new UserException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            if(userRepo.existsByMobileNumber(userDTO.getMobileNumber())){
                log.error("User with mobile number {} already exists", userDTO.getMobileNumber());
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
                savedUploadHistory1.setUploadStatus(UploadStatus.SUCCESS);
                uploadHistoryService.save(savedUploadHistory1);
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
            log.error("Exit : ErrorRecordHandler.deleteErrorRecordByEmail");
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
        log.info("Enter: ErrorRecordHandler.hasNoErrors");
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
