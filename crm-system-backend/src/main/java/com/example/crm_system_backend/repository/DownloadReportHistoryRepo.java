package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.downloadReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadReportHistoryRepo extends JpaRepository<downloadReport, Long> {
}

