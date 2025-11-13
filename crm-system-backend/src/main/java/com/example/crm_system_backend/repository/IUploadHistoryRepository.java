package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.UploadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUploadHistoryRepository extends JpaRepository<UploadHistory,String> {
    Optional<List<UploadHistory>> findByUploadedBy(String email);
}
