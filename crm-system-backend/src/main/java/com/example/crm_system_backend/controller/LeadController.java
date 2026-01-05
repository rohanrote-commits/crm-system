package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.dto.LeadDto;

import com.example.crm_system_backend.handler.LeadHandler;
import com.example.crm_system_backend.utils.GeneralUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crm/leads")
@Tag(name = "Lead Management", description = "APIs for managing leads in the CRM system")
public class LeadController {

    private static final Logger log = LoggerFactory.getLogger(LeadController.class);

    private final LeadHandler leadHandler;


    @Autowired
    public LeadController(LeadHandler leadHandler) {
        this.leadHandler = leadHandler;
    }


    /**
     * Retrieves a list of leads based on optional filters. If a user ID is provided,
     * the leads associated with that specific user will be returned. If an email is provided,
     * leads associated with the provided email will be retrieved. When no filters are specified,
     * all available leads are returned.
     *
     * @param userId Optional filter to fetch leads associated with a specific user ID.
     * @param email  Optional filter to fetch leads associated with a specific email address.
     * @return A response entity containing a list of {@code LeadDto} objects that match the specified filters.
     *         If no filters are provided, all leads are returned.
     * @author Akshay Jadhav
     */
    @Operation(summary = "Get all leads", description = "Retrieves leads based on optional filters (userId or email)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leads"),
            @ApiResponse(responseCode = "404", description = "No leads found")
    })
    @GetMapping
    public ResponseEntity<List<LeadDto>> getAllLeads(
            @Parameter(description = "User ID to filter leads") @RequestParam(required = false) Long userId,
            @Parameter(description = "Email to filter leads") @RequestParam(required = false) String email) {

        log.info("Enter: LeadController.getAllLeads");
        long uid = GeneralUtils.unmaskOnId(userId);

        if (uid != 0) {
            log.info("LeadController.getLeads By User ID: {}", uid);
            return ResponseEntity.ok(leadHandler.getLeadsByUser(uid));
        }

        if (email != null) {
            log.info("LeadController.getLeads By Email: {}", email);
            return ResponseEntity.ok(leadHandler.getLeadsByUserEmail(email));
        }
        log.info("Exit: LeadController.getAllLeads");
        return ResponseEntity.ok(leadHandler.getAll());
    }


    /**
     * Handles the creation of a new lead by saving the provided lead details.
     *
     * @param leadDto The data transfer object containing the details of the lead to be created.
     *                This must be a valid and complete lead object.
     * @return A {@code ResponseEntity} containing the created {@code LeadDto} object and an HTTP status
     *         of {@code 201 Created}.
     * @author Akshay Jadhav
     */
    @Operation(summary = "Create new lead", description = "Creates a new lead with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lead created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<LeadDto> saveLead(@Valid @RequestBody LeadDto leadDto) {
        log.info("Enter: LeadController.saveLead");
        return new ResponseEntity<>(leadHandler.save(leadDto), HttpStatus.CREATED);
    }


    /**
     * Updates an existing lead with the provided details.
     *
     * @param id The unique identifier of the lead to be updated.
     * @param leadDto The data transfer object containing the updated details of the lead.
     *                This must be a valid and complete lead object.
     * @return A {@code ResponseEntity} containing the updated {@code LeadDto} object and an HTTP status of {@code 200 OK}.
     * @author Akshay Jadhav
     */
    @Operation(summary = "Update lead", description = "Updates an existing lead with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead updated successfully"),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LeadDto> updateLead(
            @Parameter(description = "ID of the lead to update") @PathVariable Long id,
            @Valid @RequestBody LeadDto leadDto) {
        log.info("Enter: LeadController.updateLead");
        return ResponseEntity.ok(leadHandler.edit(id, leadDto));
    }


    /**
     * Deletes a lead with the given unique identifier.
     *
     * @param id The unique identifier of the lead to be deleted.
     * @return A {@code ResponseEntity} with a success message and an HTTP status of {@code 200 OK}.
     *         Indicates that the lead has been successfully deleted.
     * @author Akshay Jadhav
     */
    @Operation(summary = "Delete lead", description = "Deletes a lead by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Lead not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(
            @Parameter(description = "ID of the lead to delete") @PathVariable Long id) {
        log.info("Enter: LeadController.deleteLead");
        leadHandler.delete(id);
        log.info("Exit: LeadController.deleteLead");
        return ResponseEntity.ok("Lead deleted successfully");
    }


    /**
     * Handles the bulk import of leads from a file provided by the user.
     *
     * @param file   The multipart file containing the leads data to be imported.
     * @param userId The unique identifier of the user performing the bulk import operation.
     * @return A {@code ResponseEntity} object indicating the success of the bulk upload operation
     *         along with a confirmation message.
     * @author Akshay Jadhav
     */
    @Operation(summary = "Bulk import leads", description = "Imports multiple leads from a file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk import successful"),
            @ApiResponse(responseCode = "400", description = "Invalid file or data")
    })
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkImport(
            @Parameter(description = "File containing lead data") @RequestParam MultipartFile file,
            @Parameter(description = "ID of the user performing the import") @RequestParam Long userId) {

        log.info("Enter: LeadController.bulkImport");
        Long uid = GeneralUtils.unmaskOnId(userId);
        leadHandler.bulkUpload(file, userId);
        log.info("Exit: LeadController.bulkImport");
        return ResponseEntity.ok("Bulk upload successful");
    }


    /**
     * Updates the status of a lead identified by its unique identifier.
     *
     * @param id The unique identifier of the lead whose status needs to be updated.
     * @param body A map containing the new status of the lead. The expected key is "status",
     *             and the value represents the status code.
     * @return A {@code ResponseEntity} containing a map with the updated lead status
     *         and an HTTP status of {@code 200 OK}.
     * @author Akshay Jadhav
     */
    @Operation(summary = "Update lead status", description = "Updates the status of an existing lead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateLeadStatus(
            @Parameter(description = "ID of the lead") @PathVariable Long id,
            @Parameter(description = "New status value") @RequestBody Map<String, Integer> body) {
        log.info("Enter: LeadController.updateLeadStatus");
        Integer status = body.get("status");
        LeadStatus updatedStatus = leadHandler.updateLeadStatus(id, status);
        Map<String, Object> response = new HashMap<>();
        response.put("leadStatus", updatedStatus.getValue());

        log.info("Exit: LeadController.updateLeadStatus");
        return ResponseEntity.ok(response);
    }
}
