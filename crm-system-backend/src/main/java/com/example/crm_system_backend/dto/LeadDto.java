package com.example.crm_system_backend.dto;


import com.example.crm_system_backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class LeadDto {
    private Long id;
    private String firstName;
    private String lastName;
    @Email(message = "Email should be valid")
    private String email;
    @NotNull
    private String mobileNumber;
    @NotEmpty
    private String gstin;
    private String description;
    private String businessAddress;
    private User user;
    private Set<String> interestedModules = new HashSet<>();
}
