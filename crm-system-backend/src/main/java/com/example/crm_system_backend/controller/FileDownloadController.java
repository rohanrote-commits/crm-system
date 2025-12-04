package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.exception.FileDownloadException;
import com.example.crm_system_backend.handler.DownloadHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/crm/files")
@Tag(name = "File Download", description = "API endpoints for downloading template files")
public class FileDownloadController {
    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);
    private final DownloadHandler downloadHandler;

    public FileDownloadController(DownloadHandler downloadHandler) {
        this.downloadHandler = downloadHandler;
    }


    /**
     * Downloads the user
     * template file and returns it as a response entity
     * to allow the client to download it. The file is returned as a byte array
     * with appropriate response headers.
     *
     * @return a {@code ResponseEntity<byte[]>} containing the user template file
     *         with the file name set as "user-template.xlsx" and the Content-Type
     *         set as "application/octet-stream".
     * @throws FileDownloadException if an error occurs during file download.
     */
    @Operation(
            summary = "Download user template",
            description = "Downloads the Excel template file for user data",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template file downloaded successfully",
                            content = @Content(mediaType = "application/octet-stream")),
                    @ApiResponse(responseCode = "500", description = "Internal server error during file download")
            })
    @GetMapping("/user-template")
    public ResponseEntity<byte[]> downloadUserTemplate() throws FileDownloadException {
        log.info("Enter: FileDownloadController.downloadUserTemplate");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloadHandler.downloadUserTemplate());
    }

    /**
     * Downloads the lead template file and returns it as a response entity
     * to facilitate downloading by the client. The file is returned as a byte
     * array with appropriate response headers to indicate it as an Excel file.
     *
     * @return a {@code ResponseEntity<byte[]>} containing the lead template file,
     *         with the file name set as "lead-template.xlsx" and the Content-Type
     *         set as "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".
     * @throws FileDownloadException if an error occurs during the file download process.
     * @author akshay Jadhav
     */
    @Operation(
            summary = "Download lead template",
            description = "Downloads the Excel template file for lead data",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template file downloaded successfully",
                            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                    @ApiResponse(responseCode = "500", description = "Internal server error during file download")
            })
    @GetMapping("/lead-template")
    public ResponseEntity<byte[]> downloadLeadTemplate() throws FileDownloadException {
        log.info("Enter: FileDownloadController.downloadLeadTemplate");
        byte[] fileBytes = downloadHandler.downloadLeadTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lead-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }
}
