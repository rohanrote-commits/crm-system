package com.example.crm_system_backend.beans;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvalidUserError {
    private Integer rowNumber;
    private User user;
    private Map<String, String> errors;
}
