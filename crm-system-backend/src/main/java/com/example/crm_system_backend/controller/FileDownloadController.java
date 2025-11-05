package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.exception.FileDownloadException;
import com.example.crm_system_backend.handler.DownloadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crm/files")
public class FileDownloadController {
    @Autowired
    private DownloadHandler downloadHandler;

    @GetMapping("/user-template")
    public ResponseEntity<byte[]> downloadUserTemplate() throws FileDownloadException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloadHandler.downloadUserTemplate());
    }

    @GetMapping("/lead-template")
    public ResponseEntity<byte[]> downloadLeadTemplate() throws FileDownloadException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lead-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloadHandler.downloadUserTemplate());
    }
}
