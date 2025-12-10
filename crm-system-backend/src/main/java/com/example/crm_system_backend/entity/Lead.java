package com.example.crm_system_backend.entity;


import com.example.crm_system_backend.constants.LeadStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import javax.management.relation.Role;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Lead entity which stores information about a potential customer or client.
 * This includes personal details, business information, lead status, associated user, and interested products.
 * The entity is mapped to the "leads" table in the database.
 *
 * The class uses annotations for validation, persistence, and relationship mappings.
 * It provides fields for storing information such as name, email, mobile number,
 * description, business address, and timestamps for creation and updates.
 *
 * Fields:
 * - id: The unique identifier for the lead.
 * - firstName: The first name of the lead.
 * - lastName: The last name of the lead.
 * - email: The email address of the lead, must be valid.
 * - mobileNumber: The contact number of the lead, cannot be null.
 * - gstin: The GST identification number of the lead, must not be empty.
 * - description: Additional details or notes about the lead, maximum length is 1000 characters.
 * - businessAddress: The business address associated with the lead.
 * - user: The user to whom the lead is assigned (Many-to-One relationship).
 * - leadStatus: The current status of the lead as an enumeration.
 * - interestedProducts: A set of products the lead is interested in (Many-to-Many relationship).
 * - createdAt: The date and time when the lead entity was created.
 * - updatedAt: The date and time when the lead entity was last updated.
 *
 * Table Information:
 * - Table name: "leads".
 * - Unique constraint: The email field must be unique.
 *
 * Relationships:
 * - Many-to-One with User: Links the lead to a specific user who manages it.
 * - Many-to-Many with Product: Represents the products that the lead expresses interest in.
 *
 * Annotations:
 * - Validation: Includes constraints like @Email, @NotNull, and @NotEmpty.
 * - JPA mappings: Includes @Entity, @Id, @GeneratedValue, @JoinColumn, @Enumerated, etc.
 * - Lombok's @Data: Automatically generates getters, setters, equals, hashCode, and toString methods.
 *
 * Note: The creation and update timestamps are not automatically managed and need to be set manually.
 *
 * Author: Akshay Jadhav
 */
@Data
@Entity
@Table(name = "leads",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email"})
)
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
//    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    private LeadStatus leadStatus;

    @ManyToMany
    @JoinTable(
            name = "lead_products",
            joinColumns = @JoinColumn(name = "lead_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> interestedProducts = new HashSet<>();
    private Date createdAt;
    private Date updatedAt;

    public Lead() {}

    // Constructor for Testing ONLY

    public Lead(Long id, Date createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public Lead(Long id, Date createdAt, User user) {
        this.id = id;
        this.createdAt = createdAt;
        this.user = user;
    }

    public Lead(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Lead(Long id, String email, User user) {
        this.id = id;
        this.email = email;
        this.user = user;
    }

}

