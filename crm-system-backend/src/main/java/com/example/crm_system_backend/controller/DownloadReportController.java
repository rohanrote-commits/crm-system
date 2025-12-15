package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.exception.ReportException;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@RequestMapping("/crm/report")
@Tag(name="Download Report History", description = "Fetch the data from download_history table which contains the history of all the downloaded records till date")
public class DownloadReportController {

    @Autowired
    ReportExcelHelper helper;

    public static final Logger LOGGER = Logger.getLogger(DownloadReportController.class.getName());

    /**
     * Retrieves the download history records filtered based on the role, user ID, and email
     * of the logged-in user. This endpoint is accessible via HTTP GET request at the
     * "/getDownloadedRecordHistory" endpoint.
     * @param request the HTTP servlet request containing attributes such as role, userId,
     *                and email of the logged-in user
     * @return a ResponseEntity containing a set of filtered downloadReport objects
     *         representing the user's download history
     */
    @Operation(
            summary = "Downloads report history",
            description = "Downloads and writes the history of report templates downloaded from start date to end date in data table "
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the list of downloaded records",
                    content = @Content(mediaType = "application/json")
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

    @GetMapping("/getDownloadedRecordHistory")
    public ResponseEntity<Set<downloadReport>> getAllHistory(HttpServletRequest request) {

        Object role = request.getAttribute("role");
        Object id = request.getAttribute("userId");
        Object email = request.getAttribute("email");

        String loggedInUserRole = (String) role;
        Long loggedInUserId = (Long) id;
        String loggedInUserEmail = (String) email;

//       TODO : Use DTO class instead of entity class to transfer the data to UI.
        Set<downloadReport> filteredHistoryRecords = helper.getFilteredDownloadHistory(loggedInUserId, loggedInUserRole, loggedInUserEmail);
        LOGGER.log(Level.INFO, "Generated filtered download history of logged in user");
        return ResponseEntity.ok(filteredHistoryRecords);
    }
}
