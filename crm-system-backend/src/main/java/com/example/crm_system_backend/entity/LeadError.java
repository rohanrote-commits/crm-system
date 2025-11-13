package com.example.crm_system_backend.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class LeadError {

    private Lead lead;
    private Map<String, String> fieldErrors = new HashMap<>();

    public LeadError(Lead lead) {
        this.lead = lead;
    }

    public void addError(String field, String message) {
        fieldErrors.put(field, message);
    }

    public String getErrorForField(String field) {
        return fieldErrors.get(field);
    }

}
