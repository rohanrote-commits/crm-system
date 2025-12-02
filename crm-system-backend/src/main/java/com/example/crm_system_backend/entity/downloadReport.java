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

    @NotBlank
    private String userName;
    @NotBlank
    private String downloadedAt;
//    @NotBlank
//    private String dateOfDownload;
//    @NotBlank
//    private String timeOfDownload;
//    @NotNull
//    private String role;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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
}
