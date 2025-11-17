package com.example.crm_system_backend.dto;

import com.example.crm_system_backend.entity.Lead;

import java.util.List;

public class ErrorRecordDto {
    private String id;
    private String fileName;
    private String uplodedBy;
    private String uploadHistoryId;
    private List<Lead> errorsList;
}
