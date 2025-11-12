package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.service.Report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private ReportService service;

    @Autowired
    private ReportExcelHelper helper;

    @GetMapping("/getTemplate")
    public ResponseEntity<byte[]> getTemplate(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) throws IOException {
        List<User> userList = helper.getUserList(start, end);
        byte[] excelBytes = null;
        if (!userList.isEmpty()) {
            excelBytes = service.ListToExcel(userList, start, end);
        }
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Report Template.xlsx");
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}

