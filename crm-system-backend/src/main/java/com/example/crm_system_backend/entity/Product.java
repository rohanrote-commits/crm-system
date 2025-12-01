package com.example.crm_system_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product")
@Data

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String productName; // Example: GSTR, Accounting, Billing

    public String getProductName() {
        return productName;
    }
}