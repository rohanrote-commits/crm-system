package com.example.crm_system_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "download-history")
public class downloadReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO : Use userId.
    @NotBlank
    private String userName;
    @NotBlank
    private String downloadedAt;
    private String dateRange;
    private String status;
    private String email;

    public String toString(){
        return "\nID: " + id +
                "\nUser Email: " + userName +
                "\nDownloaded At: " + downloadedAt +
                "\ndateRange: " + dateRange +
                "\nstatus: " + status;
    }

    public downloadReport(){}

    public downloadReport(Long id, String email){
        this.id = id;
        this.email = email;
    }
}
