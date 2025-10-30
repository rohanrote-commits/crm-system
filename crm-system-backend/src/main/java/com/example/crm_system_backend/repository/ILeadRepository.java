package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ILeadRepository extends JpaRepository<Lead,Integer> {
}
