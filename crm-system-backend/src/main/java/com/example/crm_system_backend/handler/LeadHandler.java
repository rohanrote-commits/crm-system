package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.LeadStatus;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.helper.LeadExcelHelper;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class LeadHandler implements IHandler<LeadDto> {

    private final LeadService leadService;
    private final UserService userService;

    private final LeadExcelHelper  leadExcelHelper;

    public LeadHandler(LeadService leadService, UserService userRepo, LeadExcelHelper leadExcelHelper) {
        this.leadService = leadService;
        this.userService = userRepo;
        this.leadExcelHelper = leadExcelHelper;
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
        LeadDto savedLeadDto = new LeadDto();
        BeanUtils.copyProperties(savedLead, savedLeadDto);
        return savedLeadDto;
    }

    public List<LeadDto> getLeadsByUser(Long userId) {
        User user = userService.getUserById(userId).orElseThrow(
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

    public Lead getLeadByEmail(String email){
        return leadService.getLeadByEmail(email).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        );
    }

    @Override
    public List<LeadDto> getAll() {
      List<LeadDto> leadList =  leadService.getAllLeads().stream().map(lead -> {
            LeadDto leadDto = new LeadDto();
            BeanUtils.copyProperties(lead, leadDto);
            return leadDto;
        }).toList();
        return leadList;
    }

    @Override
    public LeadDto edit(Long leadId, LeadDto leadDto) {
        Lead oldLead = leadService.getLeadById(leadId).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        );
        Lead lead = new Lead();
        BeanUtils.copyProperties(oldLead, lead);
        Lead updatedLead =  leadService.editLead(leadId,lead);
        LeadDto updatedLeadDto = new LeadDto();
        BeanUtils.copyProperties(updatedLead, updatedLeadDto);
        return updatedLeadDto;
    }

    @Override
    public void delete(Long leadId) {
        leadService.deleteLead(leadId);
    }

    @Override
    public void bulkUpload(MultipartFile file,Long userId) {
        try {
            User user = userService.getUserById(userId).orElseThrow(
                    ()->   new UserException(ErrorCode.USER_NOT_FOUND)
            );
            List<Lead> leadList = leadExcelHelper.processExcelData(file);
            leadList.stream().forEach(lead -> {
                lead.setCreatedAt(new Date());
                lead.setUpdatedAt(new Date());
                lead.setLeadStatus(LeadStatus.ADDED);
                lead.setUser(user);
            });
            leadService.bulkUpload(leadList);
        }
        catch (Exception e){
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
