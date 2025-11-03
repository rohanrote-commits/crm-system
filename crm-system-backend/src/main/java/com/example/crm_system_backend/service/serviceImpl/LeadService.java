package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.ILeadService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class LeadService implements ILeadService {

    private final ILeadRepository leadRepository;
    private final IUserRepo userRepo;


    public LeadService(ILeadRepository leadRepository, IUserRepo userRepo) {
        this.leadRepository = leadRepository;
        this.userRepo = userRepo;
    }

    @Override
    public Lead save(Lead lead) {
       return leadRepository.save(lead);
    }

    @Override
    public Optional<List<Lead>> getLeadsByUser(User user) {
        return leadRepository.getLeadsByUser(user);
    }

    @Override
    public List<Lead> getAllLeads() {
       return leadRepository.findAll();
    }

    @Override
    public Lead editLead(Long leadId,Lead lead) {
        Lead savedLead = leadRepository.getLeadsById(leadId).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        );
        BeanUtils.copyProperties(lead,savedLead);
        return leadRepository.save(savedLead);
    }

    @Override
    public void deleteLead(Long leadId) {
        leadRepository.deleteById(Math.toIntExact(leadId));
    }

    @Override
    public void bulkUpload() {

    }

    public Optional<Lead> getLeadById(Long leadId) {
        return leadRepository.getLeadsById(leadId);
    }

    public Optional<Lead> getLeadByEmail(String email) {
        return leadRepository.getLeadsByEmail(email);
    }
}
