package com.example.crm_system_backend.entity;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document
@Data
public class ErrorRecord {
    @Id
    private String id;
    private String fileName;
    private String uplodedBy;
    private String uploadHistoryId;
    private List<Lead> errorsList;
}
