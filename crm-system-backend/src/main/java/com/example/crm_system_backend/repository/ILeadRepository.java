package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ILeadRepository extends JpaRepository<Lead,Integer> {
    Optional<List<Lead>> getLeadsByUser(User user);
    Optional<Lead> getLeadsById(Long leadId);
    Optional<Lead> getLeadsByEmail(String email);

    @Query("Select l.user from Lead l where l.createdAt = :date")
    Optional<User> getLeadsByDate(@Param("date") Date date);

    @Query("Select Count(l) from Lead l where l.user.id = :userId")
    int getLeadCountByUserId(@Param("userId") Long userId);

    @Query("Select l.leadStatus from Lead l where l.user.id = :userid")
    List<String> getLeadStatusByUserId(@Param("userid") Long userid);
}
