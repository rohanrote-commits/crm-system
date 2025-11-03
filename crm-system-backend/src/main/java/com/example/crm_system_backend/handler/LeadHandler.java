package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeadHandler implements IHandler<LeadDto> {

    private final LeadService leadService;
    private final IUserRepo userRepo;

    public LeadHandler(LeadService leadService, IUserRepo userRepo) {
        this.leadService = leadService;
        this.userRepo = userRepo;
    }

    @Override
    public LeadDto save(LeadDto leadDto) {
         leadService.getLeadByEmail(leadDto.getEmail()).ifPresent(
                 lead -> {
                     throw new LeadException(ErrorCode.LEAD_ALREADY_EXISTS);
                 }
         );
        Lead lead = new Lead();
        BeanUtils.copyProperties(leadDto, lead);
        Lead savedLead =  leadService.save(lead);
        LeadDto savedLeadDto = new LeadDto();
        BeanUtils.copyProperties(savedLead, savedLeadDto);
        return savedLeadDto;
    }

    public List<LeadDto> getLeadsByUser(Long userId) {
        User user = userRepo.getUserById(userId).orElseThrow(
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
    public void bulkUpload() {

    }
}
