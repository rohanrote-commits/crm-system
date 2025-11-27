package com.example.crm_system_backend.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class downloadReportDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String userName;
    @NotBlank
    private String downloadedAt;
    @NotBlank
    private String dateRange;
    private String status;
    private String email;

    public String toString(){
        return "\nID: " + id +
                "\nUser Email: " + userName +
                "\nDownloaded At: " + downloadedAt +
                "\nstartRange: " + dateRange +
                "\nstatus: " + status;
    }
}
