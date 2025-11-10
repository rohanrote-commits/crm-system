package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.ReportType;
import com.example.crm_system_backend.entity.User;
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

    @GetMapping("/getSummaryReport")
    public ResponseEntity<byte[]> getSummaryReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) throws IOException {

        // 1. Fetch Data
        List<User> userList = service.getUserList(start, end);

        // 2. Generate file bytes
        byte[] excelBytes = service.ListToExcel(userList, start, end, null);

        // 3. Set headers and return file
        HttpHeaders headers = new HttpHeaders();

        // Without it: The browser would default to interpreting the binary data as plain text (text/plain) or HTML (text/html).
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        // Without it: The browser would try to render the response inline (within the current browser window) based on the Content-Type.
        // Since Excel files cannot be rendered directly, this would either lead to an error or a failed attempt to display the data.
        // "attachment": This keyword forces the browser to treat the response as a downloadable attachment, prompting the user to save
        // the file.
        //"Report.xlsx": This value provides the browser with the suggested file name to use in the "Save As" dialog box.
        headers.setContentDispositionFormData("attachment", "Report.xlsx");

        // When the browser receives this response, it reads the headers, sees the Content-Disposition: attachment, and automatically
        // prompts the user to save the included excelBytes data as a file named Report.xlsx
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    @GetMapping("/getReport")
    public ResponseEntity<byte[]> getPerUserReport(@RequestParam String email) throws IOException {

        List<Lead> leads = service.getLeadList(email);

        byte[] excelBytes = service.ListToExcel(null, null, null, leads);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Report.xlsx");

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}

