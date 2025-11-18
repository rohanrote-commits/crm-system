package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.LeadList;
import com.example.crm_system_backend.constants.*;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.helper.LeadExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




@Component
@AllArgsConstructor
public class LeadHandler implements IHandler<LeadDto> {

    private static final Logger log = LoggerFactory.getLogger(LeadHandler.class);
    private final LeadService leadService;
    private final UserService userService;

    private final LeadExcelHelper  leadExcelHelper;

    private final ModelMapper modelMapper;

    private final UploadHistoryService uploadHistoryService;

    private final ErrorRecordHandler errorRecordHandler;




    @Override
    public LeadDto save(LeadDto leadDto) {
        log.info("Enter: LeadHandler.save");
         leadService.getLeadByEmail(leadDto.getEmail()).ifPresent(
                 lead -> {
                     throw new LeadException(ErrorCode.LEAD_ALREADY_EXISTS);
                 }
         );
        Lead savedLead =  leadService.save(leadDto);
        return modelMapper.map(savedLead,LeadDto.class);
    }


    public List<LeadDto> getLeadsByUser(Long userId) {
        log.info("Enter: LeadHandler.getLeadsByUser");
        User mainUser = userService.getUserById(userId)
                .orElseThrow(() -> {
                    log.error("LeadHandler.getLeadsByUser: User not found");
                    return new UserException(ErrorCode.USER_NOT_FOUND);
                });
        List<User> subUsers = userService.getAllUsersRegisterById(userId)
                .orElse(new ArrayList<>());
        List<User> allUsers = new ArrayList<>();
        allUsers.add(mainUser);
        allUsers.addAll(subUsers);

        //Fetch all leads for all these users (ONE DB CALL)
        List<Lead> leads = leadService.findByUserIn(allUsers);
        log.info("Exit: LeadHandler.getLeadsByUser");
        return leads.stream()
                .map(lead -> modelMapper.map(lead, LeadDto.class))
                .toList();
    }

    public Lead getLeadByEmail(String email){
        log.info("Enter: LeadHandler.getLeadByEmail");
        return leadService.getLeadByEmail(email).orElseThrow(
                ()-> {
                    log.error("Exit: LeadHandler.getLeadByEmail: Lead not found");
                   throw  new LeadException(ErrorCode.LEAD_NOT_FOUND);
                }
        );
    }

    @Override
    public List<LeadDto> getAll() {
        log.info("Enter: LeadHandler.getAll");
      List<LeadDto> leadList =  leadService.getAllLeads().stream().map(lead -> {
            LeadDto leadDto = new LeadDto();
            modelMapper.map(lead, leadDto);
            return leadDto;
        }).toList();
      log.info("Exit: LeadHandler.getAll");
        return leadList;
    }

    @Override
    public LeadDto edit(Long leadId, LeadDto leadDto) {
        log.info("Enter: LeadHandler.edit");
        Lead oldLead = leadService.getLeadById(leadId).orElseThrow(
                ()-> {
                    log.error("Exit: LeadHandler.edit->lead not found");
                    return new LeadException(ErrorCode.LEAD_NOT_FOUND);
                }
        );
        modelMapper.map(leadDto, oldLead);
        oldLead.setId(leadId);
        oldLead.setUpdatedAt(new Date());
        leadService.editLead(leadId,oldLead);
        log.info("Exit: LeadHandler.edit");
        return  modelMapper.map(oldLead,LeadDto.class);
    }

    @Override
    public void delete(Long leadId) {
        log.info("Enter: LeadHandler.delete");
        leadService.deleteLead(leadId);
        log.info("Exit: LeadHandler.delete");
    }

    @Override
    public void bulkUpload(MultipartFile file,Long userId) {
        log.info("Enter: LeadHandler.bulkUpload");
        UploadHistory  uploadHistory = new UploadHistory();
        uploadHistory.setFileName(file.getOriginalFilename());
        uploadHistory.setFileTemplateType(FileTemplateType.LEAD);
        uploadHistory.setUploadStatus(UploadStatus.PROCESSING);
        uploadHistory.setUploadedAt(LocalDateTime.now());
        uploadHistory.setFileTemplateType(FileTemplateType.LEAD);
        try {
            User user = userService.getUserById(userId).orElseThrow(
                    ()->  {
                        log.error("Exit: LeadHandler.bulkUpload-> User not found");
                        return new UserException(ErrorCode.USER_NOT_FOUND);
                    }
            );
            uploadHistory.setUploadedBy(user.getEmail());
            LeadList leadList = leadExcelHelper.processExcelData(file,uploadHistory);
            List<Lead> validLeadList = leadList.getValidLeadList();
            List<Lead> invalidLeadList = leadList.getInvalidLeadList();
            if(!validLeadList.isEmpty()) {
                validLeadList.forEach(
                        lead -> {
                            lead.setCreatedAt(new Date());
                            lead.setUpdatedAt(new Date());
                            lead.setLeadStatus(LeadStatus.ADDED);
                            lead.setUser(user);
                        }
                );
                uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
               List<Lead> savedLead =  leadService.bulkUpload(validLeadList);
               if(savedLead.isEmpty()) {
                   uploadHistory.setUploadStatus(UploadStatus.FAILED);
               }
               // uploadHistoryService.save(uploadHistory);
            }
            if(!invalidLeadList.isEmpty()) {
               // uploadHistory.setUploadStatus(UploadStatus.FAILED);
               UploadHistory savedUploadHistory = uploadHistoryService.save(uploadHistory);
                ErrorRecord errorRecord = new ErrorRecord();
                errorRecord.setUplodedBy(user.getEmail());
                errorRecord.setUploadHistoryId(savedUploadHistory.getId());
                errorRecord.setErrorsList(invalidLeadList);
                errorRecord.setFileName(file.getOriginalFilename());
                errorRecordHandler.saveErrorRecord(errorRecord);
            }
        }
        catch (Exception e){
            log.error("Exit: LeadHandler.bulkUpload {exception}",e );
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            uploadHistory.setUploadedAt(LocalDateTime.now());
            uploadHistoryService.save(uploadHistory);
            throw new LeadException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
        log.info("Exit: LeadHandler.bulkUpload");
    }

    public List<LeadDto> getLeadsByUserEmail(String email) {
        log.info("Enter: LeadHandler.getLeadsByUserEmail");
        User user = userService.getUserByEmail(email).orElseThrow(

                ()-> {
                    log.error("Exit: LeadHandler.getLeadsByUserEmail -> User not found");
                   return new UserException(ErrorCode.USER_NOT_FOUND);
                }
        );
        List<LeadDto> leadList =  leadService.getLeadsByUser(user).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        ).stream().map(lead -> {
            LeadDto leadDto = new LeadDto();
            BeanUtils.copyProperties(lead, leadDto);
            return leadDto;
        }).toList();
        log.info("Exit: LeadHandler.getLeadsByUserEmail");
        return leadList;
    }
}
