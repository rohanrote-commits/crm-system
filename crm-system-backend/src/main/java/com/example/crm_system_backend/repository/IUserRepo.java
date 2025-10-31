package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepo extends JpaRepository<User, Long> {

    Optional<User> getUserById(Long id);

    boolean findByRole(Roles role);

    boolean findByEmail(String email);

    boolean findByMobileNumber(String mobileNumber);

    Optional<User> findByEmailAndPassword(String email, String password);
}
