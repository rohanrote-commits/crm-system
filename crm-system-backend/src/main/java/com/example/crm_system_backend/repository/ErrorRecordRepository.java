package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.ErrorRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ErrorRecordRepository extends MongoRepository<ErrorRecord,String> {
    List<ErrorRecord> findAllByUploadHistoryId(String uploadHistoryId);

    Optional<ErrorRecord> findByUploadHistoryId(String id);

    Optional<ErrorRecord> findErrorRecordById(String id);
}
