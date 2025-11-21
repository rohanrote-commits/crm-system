package com.example.crm_system_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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