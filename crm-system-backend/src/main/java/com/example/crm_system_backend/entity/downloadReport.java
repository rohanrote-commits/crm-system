package com.example.crm_system_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;
    private String status;
    private String email;

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
