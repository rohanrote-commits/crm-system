package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.LeadHandler;
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
@RequestMapping("/crm/lead")
public class LeadController {

    private static final Logger log = LoggerFactory.getLogger(LeadController.class);
    private final LeadHandler  leadHandler;
    @Autowired
    public LeadController(LeadHandler leadHandler) {
        this.leadHandler = leadHandler;
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeadDto>> getAllLeads() {
        log.info("Enter: LeadController.getAllLeads");
        return new ResponseEntity<>(leadHandler.getAll(), HttpStatus.OK);

    }

    @PostMapping("/")
    public ResponseEntity<LeadDto> saveLead(@Valid @RequestBody LeadDto leadDto) {
        log.info("Enter: LeadController.saveLead");
        return new ResponseEntity<>(leadHandler.save(leadDto), HttpStatus.OK);
    }

    @GetMapping("/by/{userId}")
    public ResponseEntity<List<LeadDto>> getAllLeadsByUser(@PathVariable Long userId) {
        log.info("Enter: LeadController.getAllLeadsByUser");
        return new ResponseEntity<>(leadHandler.getLeadsByUser(userId), HttpStatus.OK);
    }

    @GetMapping("/by/email/{email}")
    public ResponseEntity<List<LeadDto>> getAllLeadsByUserByEmail(@PathVariable String email) {
        log.info("Enter: LeadController.getAllLeadsByUserByEmail");
        return new ResponseEntity<>(leadHandler.getLeadsByUserEmail(email), HttpStatus.OK);
    }

    @PutMapping("/{email}")
    public ResponseEntity<LeadDto> updateLead(@PathVariable String email ,@Valid @RequestBody LeadDto leadDto) {
        log.info("Enter: LeadController.updateLead");
        Lead lead = leadHandler.getLeadByEmail(email);
        return new ResponseEntity<>(leadHandler.edit(lead.getId(),leadDto), HttpStatus.OK);
    }

    @DeleteMapping("/")
    public ResponseEntity<?>  deleteLead(@RequestParam String email) {
        log.info("Enter: LeadController.deleteLead");
        Lead lead = leadHandler.getLeadByEmail(email);
        leadHandler.delete(lead.getId());
        log.info("Exit: LeadController.deleteLead");
        return new ResponseEntity<>("Lead Deleted Successfully",HttpStatus.OK);
    }

    @PostMapping("/import/{id}")
    public ResponseEntity<?> bulkSaveLead(@RequestParam MultipartFile file,@PathVariable Long id) {
        log.info("Enter: LeadController.bulkSaveLead");
         leadHandler.bulkUpload(file,id);
         log.info("Exit: LeadController.bulkSaveLead");
        return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
    }

    @PutMapping("/status/{status}/{email}")
    public ResponseEntity<?> updateLeadStatus(@PathVariable String email,@PathVariable Integer status) {
        Map<String, Object> response = new HashMap<>();
        LeadStatus updatedStatus = leadHandler.updateLeadStatus(email, status);
        response.put("leadStatus", updatedStatus.getValue() ) ;
        return ResponseEntity.ok(response);
    }

}
