package com.example.crm_system_backend.entity;


import com.example.crm_system_backend.constants.LeadStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
//    @ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.EAGER)
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

    public Lead() {

    }

    // Constructor for Testing ONLY
    public Lead(Long id, Date createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}

