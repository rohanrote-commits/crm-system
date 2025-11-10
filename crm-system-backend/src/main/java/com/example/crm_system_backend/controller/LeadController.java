package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.handler.LeadHandler;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/crm/lead")
public class LeadController {

    private final LeadHandler  leadHandler;
    @Autowired
    public LeadController(LeadHandler leadHandler) {
        this.leadHandler = leadHandler;
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeadDto>> getAllLeads() {
        return new ResponseEntity<>(leadHandler.getAll(), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<LeadDto> saveLead(@Valid @RequestBody LeadDto leadDto) {
        return new ResponseEntity<>(leadHandler.save(leadDto), HttpStatus.OK);
    }

    @GetMapping("/by/{userId}")
    public ResponseEntity<List<LeadDto>> getAllLeadsByUser(@PathVariable Long userId) {
        return new ResponseEntity<>(leadHandler.getLeadsByUser(userId), HttpStatus.OK);
    }

    @GetMapping("/by/email/{email}")
    public ResponseEntity<List<LeadDto>> getAllLeadsByUserByEmail(@PathVariable String email) {
        return new ResponseEntity<>(leadHandler.getLeadsByUserEmail(email), HttpStatus.OK);
    }

    @PutMapping("/{email}")
    public ResponseEntity<LeadDto> updateLead(@PathVariable String email ,@Valid @RequestBody LeadDto leadDto) {
        Lead lead = leadHandler.getLeadByEmail(email);
        return new ResponseEntity<>(leadHandler.edit(lead.getId(),leadDto), HttpStatus.OK);
    }

    @DeleteMapping("/")
    public ResponseEntity<?>  deleteLead(@RequestParam String email) {
        Lead lead = leadHandler.getLeadByEmail(email);
        leadHandler.delete(lead.getId());
        return new ResponseEntity<>("Lead Deleted Successfully",HttpStatus.OK);
    }

    @PostMapping("/file/{id}")
    public ResponseEntity<?> bulkSaveLead(@RequestParam MultipartFile file,@PathVariable Long id) {
         leadHandler.bulkUpload(file,id);
        return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
    }


}
