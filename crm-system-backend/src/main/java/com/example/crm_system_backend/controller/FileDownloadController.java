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

    /**
     * Downloads the user template file and returns it as a response entity
     * to allow the client to download it. The file is returned as a byte array
     * with appropriate response headers.
     *
     * @return a {@code ResponseEntity<byte[]>} containing the user template file
     *         with the file name set as "user-template.xlsx" and the Content-Type
     *         set as "application/octet-stream".
     * @throws FileDownloadException if an error occurs during file download.
     */
    @GetMapping("/user-template")
    public ResponseEntity<byte[]> downloadUserTemplate() throws FileDownloadException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloadHandler.downloadUserTemplate());
    }

    /**
     * Downloads the lead template file and returns it as a response entity
     * to allow the client to download it. The file is delivered as a byte array
     * with appropriate response headers, including the file name "lead-template.xlsx"
     * and the Content-Type set as "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".
     *
     * @return a {@code ResponseEntity<byte[]>} containing the lead template file in binary format
     *         along with appropriate HTTP headers for file download.
     * @throws FileDownloadException if an error occurs while downloading the lead template file.
     */
    @GetMapping("/lead-template")
    public ResponseEntity<byte[]> downloadLeadTemplate() throws FileDownloadException {
        byte[] fileBytes = downloadHandler.downloadLeadTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lead-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }
}
