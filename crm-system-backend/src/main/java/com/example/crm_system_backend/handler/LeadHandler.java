package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.helper.LeadExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class LeadHandler implements IHandler<LeadDto> {

    private final LeadService leadService;
    private final UserService userService;

    private final LeadExcelHelper  leadExcelHelper;

    private final ModelMapper modelMapper;

    private final UploadHistoryService uploadHistoryService;

    public LeadHandler(LeadService leadService, UserService userRepo, LeadExcelHelper leadExcelHelper, ModelMapper modelMapper, UploadHistoryService uploadHistoryService) {
        this.leadService = leadService;
        this.userService = userRepo;
        this.leadExcelHelper = leadExcelHelper;
        this.modelMapper = modelMapper;
        this.uploadHistoryService = uploadHistoryService;
    }

    @Override
    public LeadDto save(LeadDto leadDto) {
         leadService.getLeadByEmail(leadDto.getEmail()).ifPresent(
                 lead -> {
                     throw new LeadException(ErrorCode.LEAD_ALREADY_EXISTS);
                 }
         );
         User user = userService.getUserByEmail(leadDto.getUser()).orElseThrow(
                 ()-> new UserException(ErrorCode.USER_NOT_FOUND)
         );
        Lead lead = new Lead();
        BeanUtils.copyProperties(leadDto, lead);
        lead.setUser(user);
        lead.setCreatedAt(new Date());
        lead.setUpdatedAt(new Date());
        lead.setLeadStatus(LeadStatus.ADDED);
        Lead savedLead =  leadService.save(lead);
        return modelMapper.map(savedLead,LeadDto.class);
    }

    public List<LeadDto> getLeadsByUser(Long userId) {
        User user = userService.getUserById(userId).orElseThrow(
                ()-> new UserException(ErrorCode.USER_NOT_FOUND)
        );
        List<LeadDto> leadList =  leadService.getLeadsByUser(user).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        ).stream().map(lead -> {
            LeadDto leadDto = new LeadDto();
            //BeanUtils.copyProperties(lead, leadDto);
            modelMapper.map(lead, leadDto);
            return leadDto;
        }).toList();
        return leadList;
    }

    public Lead getLeadByEmail(String email){
        return leadService.getLeadByEmail(email).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        );
    }

    @Override
    public List<LeadDto> getAll() {
      List<LeadDto> leadList =  leadService.getAllLeads().stream().map(lead -> {
            LeadDto leadDto = new LeadDto();
           // BeanUtils.copyProperties(lead, leadDto);
            modelMapper.map(lead, leadDto);
            return leadDto;
        }).toList();
        return leadList;
    }

    @Override
    public LeadDto edit(Long leadId, LeadDto leadDto) {
        Lead oldLead = leadService.getLeadById(leadId).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        );
        modelMapper.map(leadDto, oldLead);
        oldLead.setId(leadId);
        oldLead.setUpdatedAt(new Date());
        leadService.editLead(leadId,oldLead);
        return  modelMapper.map(oldLead,LeadDto.class);
    }

    @Override
    public void delete(Long leadId) {
        leadService.deleteLead(leadId);
    }

    @Override
    public void bulkUpload(MultipartFile file,Long userId) {
        UploadHistory  uploadHistory = new UploadHistory();
        uploadHistory.setFileName(file.getOriginalFilename());
        uploadHistory.setUploadStatus(UploadStatus.PROCESSING);
        uploadHistory.setUploadedAt(LocalDateTime.now());
        try {
            User user = userService.getUserById(userId).orElseThrow(
                    ()->   new UserException(ErrorCode.USER_NOT_FOUND)
            );
            uploadHistory.setUploadedBy(user.getEmail());
            List<Lead> leadList = leadExcelHelper.processExcelData(file,uploadHistory);
            if(!leadList.isEmpty()) {
                leadList.forEach(lead -> {
                    lead.setCreatedAt(new Date());
                    lead.setUpdatedAt(new Date());
                    lead.setLeadStatus(LeadStatus.ADDED);
                    lead.setUser(user);
                });
                leadService.bulkUpload(leadList);
                uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
                uploadHistoryService.save(uploadHistory);
            }
            else {
                uploadHistory.setUploadStatus(UploadStatus.FAILED);
                uploadHistoryService.save(uploadHistory);
                throw new LeadException(ErrorCode.FILE_PROCESSING_FAILED);
            }
        }
        catch (Exception e){
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            uploadHistoryService.save(uploadHistory);
            log.error(e.getMessage());
            e.getStackTrace();
            throw new LeadException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }

    public List<LeadDto> getLeadsByUserEmail(String email) {
        User user = userService.getUserByEmail(email).orElseThrow(
                ()-> new UserException(ErrorCode.USER_NOT_FOUND)
        );
        List<LeadDto> leadList =  leadService.getLeadsByUser(user).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        ).stream().map(lead -> {
            LeadDto leadDto = new LeadDto();
            BeanUtils.copyProperties(lead, leadDto);
            return leadDto;
        }).toList();
        return leadList;
    }
}
