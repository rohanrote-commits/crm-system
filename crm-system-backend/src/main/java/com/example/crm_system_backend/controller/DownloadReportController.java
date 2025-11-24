package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
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
    DownloadReportHistoryRepo historyRepo;

    @Autowired
    ReportExcelHelper helper;

    @Autowired
    ReportService reportService;

    public static final Logger LOGGER = Logger.getLogger(DownloadReportController.class.getName());

    /**
     * Updates necessary data and saves the newly added record in database
     * @param data Logged in User data received from UI
     * @return newly added download History object
     */
//    @PostMapping("/saveDownloadedRecord")
//    public ResponseEntity<?> downloadHistory(@RequestBody downloadReport data) {
//
//        LOGGER.log(Level.INFO, "Received request from frontend to download history");
//
//        String email = data.getUserName();
//        String role = data.getRole();
//
//        String name = helper.getName(email);
//
//        if(role.equalsIgnoreCase(Roles.MASTER_ADMIN.name())) {
//            data.setRole(Roles.MASTER_ADMIN.getDescription());
//        } else if(role.equalsIgnoreCase(Roles.ADMIN.name())) {
//            data.setRole(Roles.ADMIN.getDescription());
//        } else if(role.equalsIgnoreCase(Roles.BASIC.name()) || role.equalsIgnoreCase(Roles.USER.name())) {
//            data.setRole(Roles.BASIC.getDescription());
//        }
//
//        data.setUserName(name);
//        data.setEmail(email);
//
//        downloadReport downloadReport = historyRepo.save(data);
//        LOGGER.log(Level.INFO, "Made necessary changes in data and saved the data in database");
//        return new ResponseEntity<>(downloadReport, HttpStatus.OK);
//    }


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

        Set<downloadReport> filteredHistory = reportService.getFilteredDownloadHistory(loggedInUserId, loggedInUserRole, loggedInUserEmail);
        return ResponseEntity.ok(filteredHistory);
    }
}
