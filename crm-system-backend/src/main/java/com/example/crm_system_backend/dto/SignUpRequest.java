package com.example.crm_system_backend.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String password;
    private String confirmPassword;
   //Address information
    private String address;
    private String city;
    private String state;
    private String country;
    private String pinCode;
}
