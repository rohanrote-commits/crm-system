package com.example.crm_system_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

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
