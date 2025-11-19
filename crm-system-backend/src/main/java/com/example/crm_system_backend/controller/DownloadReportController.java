package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin("*")
@RestController
public class DownloadReportController {

    @Autowired
    private DownloadReportHistoryRepo historyRepo;

    @Autowired
    ReportExcelHelper helper;

    @PostMapping("/downloadHistory")
    public ResponseEntity<?> downloadHistory(@RequestBody downloadReport data) {

        System.out.println("\n\n Recieved data: " + data);

        String email = data.getUserName();
        String role = data.getRole();

        String name = helper.getName(email);

        if(role.equalsIgnoreCase(Roles.MASTER_ADMIN.name())) {
            data.setRole(Roles.MASTER_ADMIN.getDescription());
        } else if(role.equalsIgnoreCase(Roles.ADMIN.name())) {
            data.setRole(Roles.ADMIN.getDescription());
        } else if(role.equalsIgnoreCase(Roles.BASIC.name()) || role.equalsIgnoreCase(Roles.USER.name())) {
            data.setRole(Roles.BASIC.getDescription());
        }

//        long index = 0L;

        data.setUserName(name);
//        data.setId(++index);

        downloadReport downloadReport = historyRepo.save(data);
//        return new ResponseEntity<>(data, HttpStatus.OK);
        return new ResponseEntity<>(downloadReport, HttpStatus.OK);
    }


    @GetMapping("/downloadHistory/all")
    public ResponseEntity<List<downloadReport>> getAllHistory() {
        List<downloadReport> history = historyRepo.findAll();
        return ResponseEntity.ok(history);
    }

}
