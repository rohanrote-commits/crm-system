package com.example.crm_system_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "download-history")
public class downloadReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String userName;
    @NotBlank
    private String dateOfDownload;
    @NotBlank
    private String timeOfDownload;
    @NotNull
    private String role;
    @NotBlank
    private String startDate;
    @NotBlank
    private String endDate;

    private String status;

    public String toString(){
        return "\nID: " + id +
                "\nUser Email: " + userName +
                "\ndateOfDownload: " + dateOfDownload +
                "\ntimeOfDownload: " + timeOfDownload +
                "\nrole: " + role +
                "\nstartDate: " + startDate +
                "\nendDate: " + endDate +
                "\nstatus: " + status;
    }
}
