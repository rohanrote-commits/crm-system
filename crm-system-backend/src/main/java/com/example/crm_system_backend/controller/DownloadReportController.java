package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@RequestMapping("/crm/report")
public class DownloadReportController {

    @Autowired
    ReportExcelHelper helper;

    public static final Logger LOGGER = Logger.getLogger(DownloadReportController.class.getName());


    /**
     * Retrieves the download history records filtered based on the role, user ID, and email
     * of the logged-in user. This endpoint is accessible via HTTP GET request at the
     * "/getDownloadedRecordHistory" endpoint.
     *
     * @param request the HTTP servlet request containing attributes such as role, userId,
     *                and email of the logged-in user
     * @return a ResponseEntity containing a set of filtered downloadReport objects
     *         representing the user's download history
     */
    @GetMapping("/getDownloadedRecordHistory")
    public ResponseEntity<Set<downloadReport>> getAllHistory(HttpServletRequest request) {

        Object role = request.getAttribute("role");
        Object id = request.getAttribute("userId");
        Object email = request.getAttribute("email");

        String loggedInUserRole = (String) role;
        Long loggedInUserId = (Long) id;
        String loggedInUserEmail = (String) email;

        Set<downloadReport> filteredHistoryRecords = helper.getFilteredDownloadHistory(loggedInUserId, loggedInUserRole, loggedInUserEmail);
        LOGGER.log(Level.INFO, "Generated filtered download history of logged in user");
        return ResponseEntity.ok(filteredHistoryRecords);
    }
}
