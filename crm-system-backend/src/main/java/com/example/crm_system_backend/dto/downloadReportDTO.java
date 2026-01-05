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
    private User user;

    public downloadReportDTO() {}

    public downloadReportDTO(Long id, User userId) {
        this.id = id;
        user = userId;
    }

    public String toString(){
        return "\nID: " + id +
                "\nUsername: " + userName +
                "\nDownloaded At: " + downloadedAt +
                "\nstartRange: " + dateRange +
                "\nstatus: " + status;
    }
}
