package com.example.crm_system_backend.dto;

import com.example.crm_system_backend.constants.UploadStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UploadHistoryDto {
    private String fileName;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private UploadStatus uploadStatus;
    private String errorFileName;
}
