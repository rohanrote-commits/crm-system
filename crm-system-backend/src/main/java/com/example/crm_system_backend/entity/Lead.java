package com.example.crm_system_backend.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "lead")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Email(message = "Email should be valid")
    private String email;
    @NotNull
    private String mobileNumber;
    @NotEmpty
    private String gstin;
    @Column(length = 1000)
    private String description;

    private String businessAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "lead_interested_modules",
            joinColumns = @JoinColumn(name = "lead_id")
    )
    @Column(name = "module_name", unique = true)
    private Set<String> interestedModules = new HashSet<>();
}

