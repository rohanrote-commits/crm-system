package com.example.crm_system_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents a Product entity which is mapped to the "product" table in the database.
 * This entity is used to store product-related information, including a unique product name.
 *
 * The class uses JPA annotations for entity mapping and persistence.
 * Lombok's @Data annotation is used to generate boilerplate code such as getters, setters, equals, hashCode, and toString methods.
 *
 * Features:
 * - id: The unique identifier for the product. It is auto-generated.
 * - productName: A mandatory and unique field representing the name of the product.
 *
 * Table Information:
 * - Table name: "product".
 * - Unique constraint: The productName field is unique.
 *
 * Relationships:
 * - No explicit relationships defined in this class, but may be related to other entities like `Lead` in a Many-to-Many relationship.
 *
 * Annotations:
 * - @Entity: Specifies that the class is a JPA entity.
 * - @Table: Specifies the database table to which this entity is mapped.
 * - @Id and @GeneratedValue: Declare the primary key and its generation strategy.
 * - @Column: Specifies database column details such as uniqueness and null constraints.
 * - Lombok's @Data: Reduces boilerplate code by generating commonly used methods.
 *
 * Author: Akshay Jadhav
 */
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