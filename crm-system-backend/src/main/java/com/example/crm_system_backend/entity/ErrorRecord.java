package com.example.crm_system_backend.entity;

import jakarta.persistence.Id;
import lombok.Data;
import java.util.List;

@Data
public class ErrorRecord {
    @Id
    private String id;
    private String fileName;
    private String uplodedBy;
    private String uploadHistoryId;
    private List<Lead> errorsList;
    private List<User> errorUserList;
}
