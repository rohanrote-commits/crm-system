package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.ErrorRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErrorRecordRepository extends MongoRepository<ErrorRecord,String> {
    List<ErrorRecord> findAllByUploadHistoryId(String uploadHistoryId);

    Optional<ErrorRecord> findByUploadHistoryId(String id);

    Optional<ErrorRecord> findErrorRecordById(String id);
}
