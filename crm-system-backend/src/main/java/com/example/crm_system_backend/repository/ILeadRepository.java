package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ILeadRepository extends JpaRepository<Lead,Integer> {
    Optional<List<Lead>> getLeadsByUser(User user);
    Optional<Lead> getLeadsById(Long leadId);
    Optional<Lead> getLeadsByEmail(String email);
}
