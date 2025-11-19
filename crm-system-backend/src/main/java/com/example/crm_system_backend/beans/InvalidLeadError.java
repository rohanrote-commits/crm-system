package com.example.crm_system_backend.beans;

import com.example.crm_system_backend.entity.Lead;
import lombok.Data;

import java.util.Map;

@Data
public class InvalidLeadError {
    private Integer rowNumber;
    private Lead lead;
    private Map<String, String> errors;
}