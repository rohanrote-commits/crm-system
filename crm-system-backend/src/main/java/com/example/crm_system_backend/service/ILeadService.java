package com.example.crm_system_backend.service;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface ILeadService {
    Lead save(Lead leadDto);
    Optional<List<Lead>> getLeadsByUser(User user);
    List<Lead> getAllLeads();
    Lead editLead(Long leadId,Lead leadDto);
    void deleteLead(Long leadId);
    void bulkUpload(List<Lead> leads);
}
