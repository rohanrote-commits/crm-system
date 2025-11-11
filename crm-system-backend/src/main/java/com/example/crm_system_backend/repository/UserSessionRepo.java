package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepo extends JpaRepository<UserSession, String> {
    boolean existsByEmail(String email);

    void deleteByEmail(String email);

    boolean existsByToken(String token);

    boolean existsByTokenAndEmail(String token, String email);
}
