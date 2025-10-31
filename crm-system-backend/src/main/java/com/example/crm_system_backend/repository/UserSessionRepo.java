package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepo extends JpaRepository<UserSession, Long> {
    boolean findByEmail(String email);

    void deleteByEmail(String email);
}
