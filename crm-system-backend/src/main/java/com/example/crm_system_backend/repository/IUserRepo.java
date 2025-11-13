package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserRepo extends JpaRepository<User, Long> {

    Optional<User> getUserById(Long id);


    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<User> findByEmailAndPassword(String email, String password);

    List<User> findUsersByRegisteredBy(Long id);

    @Query("SELECT u.role FROM user u WHERE u.id = :id")
    Roles findRoleById(@Param("id") Long id);

    @Query("SELECT u.email FROM user u WHERE u.id = :id")
    String findEmailById(@Param("id") Long id);

    Optional<User> getUserByEmail(String email);
}
