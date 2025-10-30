package com.example.crm_system_backend.entity;

import jakarta.persistence.*;

@Entity(name = "user")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true)
    private String mobileNumber;
    @Column(nullable = false, unique = true)
    private String password;
    @Column(nullable = true)
    private String address;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private String pinCode;
    @Column(nullable = false)
    private Roles role;

    private long registeredBy;



}
