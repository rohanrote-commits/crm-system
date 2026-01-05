package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.ReportConstant;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.ReportService;
import com.example.crm_system_backend.utils.GeneralUtils;
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
    ReportExcelHelper helper;

    @Autowired
    ReportService reportService;


    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());

    public ReportController(ReportService reportService, ReportExcelHelper helper) {
        this.reportService = reportService;
        this.helper = helper;
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
    public ResponseEntity<StreamingResponseBody> getTemplate(@RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                                                             @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                                                             @RequestHeader("userId") Long userId
                                                             ) {

        userId = GeneralUtils.unmaskOnId(userId);
        String email = helper.getEmailByUserId(userId);

        LOGGER.log(Level.INFO, "START : CLASS >> ReportController >> METHOD >> " +
                "getTemplate with start date: " + start + " and end date: " + end + " for user with email: " + email);

        Set<Lead> leadList = helper.getLeads(start, end);
        if(leadList.isEmpty()) {
            LOGGER.log(Level.WARNING, "CLASS >> ReportController >> METHOD >> getTemplate with start date: " + start +
                    " and end date: " + end + " for user with email: " + email + " >> " + ReportConstant.noDataText);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        LOGGER.log(Level.INFO, "INTERMEDIATE : CLASS >> ReportController >> METHOD >> " + "getTemplate >> " +
                "Successfully generated Zip file with start date: " + start + " and end date: " + end + " for user " +
                "with email: " + email);

        if(email == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            reportService.saveInDb(start, end, userId);
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, "ERROR : CLASS >> ReportController >> METHOD >> " +
                    "getTemplate with start date: " + start + " and end date: " + end + " for user with email: " + email
                    + " >> Error: ", ex);
            return new ResponseEntity<>(ErrorCode.FAILED_TO_SAVE_IN_DB.getStatus());
        }
        LOGGER.log(Level.INFO, "END : CLASS >> ReportController >> METHOD >> " +
                "getTemplate with start date: " + start + " and end date: " + end + " for user with email: " + email);
        return reportService.excelToZipConverter(leadList, start, end);
    }
}

