package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.dto.LeadDto;

import com.example.crm_system_backend.handler.LeadHandler;
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
public class LeadController {

    private static final Logger log = LoggerFactory.getLogger(LeadController.class);

    private final LeadHandler leadHandler;

    @Autowired
    public LeadController(LeadHandler leadHandler) {
        this.leadHandler = leadHandler;
    }

    /*
    * 1. GET ALL LEADS
    * @author Akshay Jadhav
    * @return List<LeadDto>
    * @param userId
    * @param email
    *
    * */
    @GetMapping
    public ResponseEntity<List<LeadDto>> getAllLeads(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email) {

        log.info("Enter: LeadController.getAllLeads");

        if (userId != null) {
            log.info("LeadController.getLeads By User ID: {}", userId);
            return ResponseEntity.ok(leadHandler.getLeadsByUser(userId));
        }

        if (email != null) {
            log.info("LeadController.getLeads By Email: {}", email);
            return ResponseEntity.ok(leadHandler.getLeadsByUserEmail(email));
        }
        log.info("Exit: LeadController.getAllLeads");
        return ResponseEntity.ok(leadHandler.getAll());
    }


    @PostMapping
    public ResponseEntity<LeadDto> saveLead(@Valid @RequestBody LeadDto leadDto) {
        log.info("Enter: LeadController.saveLead");
        return new ResponseEntity<>(leadHandler.save(leadDto), HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    public ResponseEntity<LeadDto> updateLead(@PathVariable Long id,
                                              @Valid @RequestBody LeadDto leadDto) {
        log.info("Enter: LeadController.updateLead");
        return ResponseEntity.ok(leadHandler.edit(id, leadDto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Long id) {
        log.info("Enter: LeadController.deleteLead");
        leadHandler.delete(id);
        log.info("Exit: LeadController.deleteLead");
        return ResponseEntity.ok("Lead deleted successfully");
    }


    @PostMapping("/bulk")
    public ResponseEntity<?> bulkImport(@RequestParam MultipartFile file,
                                        @RequestParam Long userId) {

        log.info("Enter: LeadController.bulkImport");
        leadHandler.bulkUpload(file, userId);
        log.info("Exit: LeadController.bulkImport");
        return ResponseEntity.ok("Bulk upload successful");
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateLeadStatus(@PathVariable Long id,
                                              @RequestBody Map<String, Integer> body) {
        log.info("Enter: LeadController.updateLeadStatus");
        Integer status = body.get("status");
        LeadStatus updatedStatus = leadHandler.updateLeadStatus(id, status);
        Map<String, Object> response = new HashMap<>();
        response.put("leadStatus", updatedStatus.getValue());

        log.info("Exit: LeadController.updateLeadStatus");
        return ResponseEntity.ok(response);
    }
}
