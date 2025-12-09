package com.example.crm_system_backend.dto;

import com.example.crm_system_backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class downloadReportDTO {
    private Long id;
    private String userName;
    private String downloadedAt;
    private String dateRange;
    private String status;
    private String email;
    private User user;

    public String toString(){
        return "\nID: " + id +
                "\nUser Email: " + userName +
                "\nDownloaded At: " + downloadedAt +
                "\nstartRange: " + dateRange +
                "\nstatus: " + status;
    }

    public downloadReportDTO(Long id, String email, User user){
        this.id = id;
        this.email = email;
        this.user = user;
    }
}
