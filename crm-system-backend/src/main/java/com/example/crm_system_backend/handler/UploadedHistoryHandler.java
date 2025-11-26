package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.FileTemplateType;
import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.entity.UploadHistory;

import com.example.crm_system_backend.exception.UserException;

import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;



@Component
@AllArgsConstructor
public class UploadedHistoryHandler {

    private static final Logger log = LoggerFactory.getLogger(UploadedHistoryHandler.class);
    private  final UploadHistoryService uploadHistoryService;

    private ModelMapper modelMapper;




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

    public UploadHistoryDto findUploadHistoryById(@PathVariable String uploadHistoryId){
      log.info("Enter : findUploadHistoryById");
     UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
     log.info("Exit : findUploadHistoryById");
     return modelMapper.map(uploadHistory,UploadHistoryDto.class);
    }
}
