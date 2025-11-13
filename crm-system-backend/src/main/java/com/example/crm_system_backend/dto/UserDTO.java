package com.example.crm_system_backend.dto;

import com.example.crm_system_backend.constants.Roles;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String password;
    private String confirmPassword;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pinCode;
    private Roles role;

    private long registeredBy;
    private String emailOfAdminRegistered;
}
