package com.example.crm_system_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_session")
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {


    @Id
    private String email;

    private String token;



}