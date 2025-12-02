package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.FileTemplateType;
import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.entity.UploadHistory;

import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.UserException;

import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;



/**
 * Handles operations related to retrieving and processing upload history records.
 * This class acts as an intermediary layer between the service layer and the external components
 * that require upload history data. It includes functionality to retrieve filtered or specific
 * upload history records based on certain criteria.
 *
 * Responsibilities:
 * - Retrieve upload history records filtered by email and template type.
 * - Map domain entities to their respective DTO representations.
 * - Handle potential exceptions during processing and log relevant information.
 *
 * Dependencies:
 * - {@code UploadHistoryService}: Provides methods to interact with upload history records in persistence.
 * - {@code ModelMapper}: Maps entities to DTOs and vice versa.
 *
 * Thread Safety:
 * - This class is thread-safe given the stateless nature of its operations and the final dependencies.
 * - Logging is used internally and should not compromise thread safety.
 *
 * Logging:
 * - This class logs method entry/exit points and any notable exceptions for debugging purposes.
 * - Ensure logging levels are appropriately configured to avoid exposing sensitive information in logs.
 *
 * Usage:
 * - Ensured through dependency injection by Spring's {@code @Component}, automating lifecycle management.
 *
 * Author:
 * - Akshay Jadhav
 */
@Component
@AllArgsConstructor
public class UploadedHistoryHandler {

    private static final Logger log = LoggerFactory.getLogger(UploadedHistoryHandler.class);
    private  final UploadHistoryService uploadHistoryService;

    private ModelMapper modelMapper;




    /**
     * Retrieves a list of upload history records, specifically those associated with the
     * {@code LEAD} file template type, for a user identified by their email address.
     *
     * @param email the email address of the user whose lead upload history is to be retrieved
     * @return a list of {@code UploadHistoryDto} objects representing the filtered lead-related
     * upload history for the specified user
     */
    public List<UploadHistoryDto> findLeadUploadHistoryByEmail(String email)
    {
        log.info("Enter : findLeadUploadHistoryByEmail");



        List<UploadHistoryDto> uploadHistoryDtos = uploadHistoryService.findByUser(email).stream().
                filter(uploadHistory -> {
                    FileTemplateType fileTemplateType = uploadHistory.getFileTemplateType();
                    return fileTemplateType != null &&
                            fileTemplateType.name().equalsIgnoreCase(FileTemplateType.LEAD.name());
                        }
                ).map(
                        uploadHistory -> modelMapper.map(uploadHistory, UploadHistoryDto.class)
                ).toList();
        log.info("Exit : findLeadUploadHistoryByEmail");
         return uploadHistoryDtos;

        }


    /**
     * Retrieves a list of upload history records for a user identified by their email.
     * The records returned are filtered to include only those associated with the {@code USER} file template type.
     *
     * @param email the email address of the user whose upload history is to be retrieved
     * @return a list of {@code UploadHistoryDto} objects representing the user's upload history
     * @throws UserException if no user is found with the provided email address
     */
    public List<UploadHistoryDto> findUserUploadHistoryByEmail(String email) {
        log.info("Enter : findUserUploadHistoryByEmail");
        try {
            List<UploadHistoryDto> uploadHistoryDtos = uploadHistoryService.findByUser(email).stream()
                    .filter(uploadHistory -> {
                        FileTemplateType templateType = uploadHistory.getFileTemplateType();
                        return templateType != null &&
                                templateType.name().equalsIgnoreCase(FileTemplateType.USER.name());
                    })
                    .map(uploadHistory -> modelMapper.map(uploadHistory, UploadHistoryDto.class))
                    .toList();
            log.info("Exit : findUserUploadHistoryByEmail");
            return uploadHistoryDtos;
        } catch (UserException e) {

            if (log.isErrorEnabled()) {
                log.error("Exception in findUserUploadHistoryByEmail", e.getMessage());
            }
            throw new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL);
        }
    }

    /**
     * Retrieves the upload history record based on the provided upload history ID.
     * This method maps the retrieved {@code UploadHistory} entity to a {@code UploadHistoryDto}.
     *
     * @param uploadHistoryId the unique identifier of the upload history record to retrieve
     * @return an {@code UploadHistoryDto} object containing the details of the upload history record
     * @throws ExcelException if no upload history record is found for the provided ID
     * @author Akshay Jadhav
     */
    public UploadHistoryDto findUploadHistoryById(@PathVariable String uploadHistoryId){
      log.info("Enter : findUploadHistoryById");
     UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
     log.info("Exit : findUploadHistoryById");
     return modelMapper.map(uploadHistory,UploadHistoryDto.class);
    }
}
