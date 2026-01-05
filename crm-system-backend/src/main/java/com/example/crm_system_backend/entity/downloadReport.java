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

    @NotNull
    private Long userId;
    @NotBlank
    private String downloadedAt;
    private String dateRange;
    private String status;

    public downloadReport(long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

    public downloadReport() {}

    public String toString(){
        return "\nID: " + id +
                "\nUser ID: " + userId +
                "\nDownloaded At: " + downloadedAt +
                "\ndateRange: " + dateRange +
                "\nstatus: " + status;
    }

}
