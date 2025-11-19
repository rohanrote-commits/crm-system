package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.service.Report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@CrossOrigin("*")

public class ReportController {

    @Autowired
    private ReportService service;

    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());

    @GetMapping("/getTemplate")
    public ResponseEntity<StreamingResponseBody> getTemplate(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) throws IOException {

        Set<Lead> leadList = service.getLeads(start, end);
        if(leadList.isEmpty()) {
            LOGGER.log(Level.WARNING, "No leads are registered in this time period.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return service.excelToZipConverter(leadList, start, end);
    }
}

