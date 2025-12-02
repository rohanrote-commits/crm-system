package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepo extends JpaRepository<User, Long> {

    Optional<User> getUserById(Long id);

    @Query("Select u from user u where u.registeredBy = :registeredBy")
    List<User> getUserByRegisteredBy(@Param("registeredBy") Long registeredBy);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<User> findByEmailAndPassword(String email, String password);

    List<User> findUsersByRegisteredBy(Long id);

    @Query("SELECT u.role FROM user u WHERE u.id = :id")
    Roles findRoleById(@Param("id") Long id);

    @Query("SELECT u.email FROM user u WHERE u.id = :id")
    String findEmailById(@Param("id") Long id);

    @Query("SELECT u.firstName FROM user u WHERE u.email = :email")
    String findUserFirstNameByEmail(@Param("email") String email);

    @Query("SELECT u.lastName FROM user u WHERE u.email = :email")
    String findUserLastNameByEmail(@Param("email") String email);

    Optional<User> getUserByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
