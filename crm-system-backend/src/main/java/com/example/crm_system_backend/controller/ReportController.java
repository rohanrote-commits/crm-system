package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
import com.example.crm_system_backend.service.serviceImpl.ReportService;
import com.example.crm_system_backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@RequestMapping("/crm/report")
public class ReportController {

    @Autowired
    private DownloadReportHistoryRepo historyRepo;

    @Autowired
    ReportExcelHelper helper;

    @Autowired
    ReportService reportService;

    @Autowired
    private JwtUtil jwtUtil;


    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());

    /**
     * Returns complete Report template including summary report and per user reports
     * @param start start date
     * @param end end date
     * @return zip file with Excel template in it
     */
    @PostMapping("/getTemplate")
    public ResponseEntity<StreamingResponseBody> getTemplate(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                                                             @RequestHeader("Authorization")String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "");

        Set<Lead> leadList = helper.getLeads(start, end);
        if(leadList.isEmpty()) {
            LOGGER.log(Level.WARNING, "No leads are registered in this time period.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        LOGGER.log(Level.INFO, "Successfully generated Zip file");

        // Save in DB
        downloadReport data = new downloadReport();

        // Access Token
        String email = jwtUtil.getEmail(token);
        String name = helper.getName(email);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startLocal = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocal = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String formattedStart = formatter.format(startLocal);
        String formattedEnd = formatter.format(endLocal);

        String dateRange = formattedStart + " To " + formattedEnd;

        data.setDateRange(dateRange);
        data.setEmail(email);
        data.setUserName(name);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        data.setDownloadedAt(now.format(formatter2));
        data.setStatus("Success");

        historyRepo.save(data);
        LOGGER.log(Level.INFO, "Made necessary changes in data and saved the data in database");

        return reportService.excelToZipConverter(leadList, start, end);
    }
}

