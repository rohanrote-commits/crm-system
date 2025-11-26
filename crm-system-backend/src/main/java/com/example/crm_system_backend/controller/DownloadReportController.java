package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.service.Report.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.logging.Logger;


@RestController
@RequestMapping("/crm/report")
public class DownloadReportController {

    @Autowired
    ReportService reportService;

    public static final Logger LOGGER = Logger.getLogger(DownloadReportController.class.getName());


    /**
     * Get History of all downloaded records according to their role
     * @param request HttpServletRequest to access token attributes
     * @return filtered download report history
     */
    @GetMapping("/getDownloadedRecordHistory")
    public ResponseEntity<Set<downloadReport>> getAllHistory(HttpServletRequest request) {

        Object role = request.getAttribute("role");
        Object id = request.getAttribute("userId");
        Object email = request.getAttribute("email");

        String loggedInUserRole = (String) role;
        Long loggedInUserId = (Long) id;
        String loggedInUserEmail = (String) email;

        Set<downloadReport> filteredHistoryRecords = reportService.getFilteredDownloadHistory(loggedInUserId, loggedInUserRole, loggedInUserEmail);
        return ResponseEntity.ok(filteredHistoryRecords);
    }
}
