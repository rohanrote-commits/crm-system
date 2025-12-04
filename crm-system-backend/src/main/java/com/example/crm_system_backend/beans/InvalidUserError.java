package com.example.crm_system_backend.beans;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import lombok.Data;

import java.util.Map;

@Data
public class InvalidUserError {
    private Integer rowNumber;
    private User user;
    private Map<String, String> errors;
}
