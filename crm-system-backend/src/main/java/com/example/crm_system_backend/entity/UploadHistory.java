package com.example.crm_system_backend.entity;

import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.constants.UploadStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Data
public class UploadHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
   private String id;
   private String fileName;
   private LocalDateTime uploadedAt;
   private String uploadedBy;
   private int totalRecords;
   @Enumerated(EnumType.STRING)
   private UploadStatus uploadStatus;
   private int validRecords;
   private  int invalidRecords;
   private String errorFileName;
}
