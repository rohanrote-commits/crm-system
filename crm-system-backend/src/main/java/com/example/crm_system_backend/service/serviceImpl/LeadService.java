package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.ILeadService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class LeadService implements ILeadService {

    private final ILeadRepository leadRepository;
    private final IUserRepo userRepo;
    private final ModelMapper modelMapper;



    @Override
    public Lead save(LeadDto leadDto) {
        User user = userRepo.getUserByEmail(leadDto.getUser()).orElseThrow(
                ()-> new UserException(ErrorCode.USER_NOT_FOUND)
        );
        Lead lead = modelMapper.map(leadDto, Lead.class);
        lead.setUser(user);
        lead.setCreatedAt(new Date());
        lead.setUpdatedAt(new Date());
        lead.setLeadStatus(LeadStatus.ADDED);
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
    public void editLead(Long leadId, Lead lead) {
        leadRepository.save(lead);
    }

    @Override
    public void deleteLead(Long leadId) {
        leadRepository.deleteById(Math.toIntExact(leadId));
    }

    @Override
    public List<Lead> bulkUpload(List<Lead> leads) {
      return leadRepository.saveAll(leads);
    }

    public Optional<Lead> getLeadById(Long leadId) {
        return leadRepository.getLeadsById(leadId);
    }

    public Optional<Lead> getLeadByEmail(String email) {
        return leadRepository.getLeadsByEmail(email);
    }

    public List<Lead> findByUserIn(List<User> allUsers) {
        return  leadRepository.findByUserIn(allUsers);
    }
}
