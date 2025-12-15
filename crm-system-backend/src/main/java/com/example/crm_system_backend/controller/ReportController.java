package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
import com.example.crm_system_backend.service.serviceImpl.ReportService;
import com.example.crm_system_backend.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@RequestMapping("/crm/report")
@Tag(name="Report Template", description = "Generates Report Template for all the leads registered in specific date range")
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


    public ReportController(ReportService reportService, ReportExcelHelper helper, JwtUtil jwtUtil) {
        this.reportService = reportService;
        this.helper = helper;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Returns complete Report template including summary report and per user reports
     * @param start start date
     * @param end end date
     * @return zip file with Excel template in it
     */
    @Operation(
            summary="Generates zip file",
            description = "Generates Report Template and convert it into zip file"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated Report Template",
                    content = @Content(mediaType = "application/zip")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request: Use correct Request type (GET/ PUT/ POST/ DELETE)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized: User Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found: The requested resource could not be found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during processing"
            )
    })

    @PostMapping("/getTemplate")
    // TODO - Do not get the email from the token. Get the masked user Id from header then unmask it get the user email from the user Id.
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

        String email;
        try {
            email = jwtUtil.getEmail(token);
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "JWT Signature failed for token", ex);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(email == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            reportService.saveInDb(start, end, email);
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "Report Template Saving failed for token", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return reportService.excelToZipConverter(leadList, start, end);
    }
}

