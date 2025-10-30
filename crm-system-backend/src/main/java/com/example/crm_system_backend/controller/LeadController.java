package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.handler.IHandler;
import com.example.crm_system_backend.handler.LeadHandler;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/crm/lead")
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

    @PutMapping("/{leadId}")
    public ResponseEntity<LeadDto> updateLead(@PathVariable Long leadId ,@Valid @RequestBody LeadDto leadDto) {
        return new ResponseEntity<>(leadHandler.edit(leadId,leadDto), HttpStatus.OK);
    }

    @DeleteMapping("/{leadId}")
    public ResponseEntity<?>  deleteLead(@PathVariable Long leadId) {
        leadHandler.delete(leadId);
        return new ResponseEntity<>("Lead Deleted Successfully",HttpStatus.OK);
    }

    @PostMapping("/file")
    public ResponseEntity<?> bulkSaveLead(@RequestParam MultipartFile file) {
        leadHandler.bulkUpload();
        return new ResponseEntity<>(file, HttpStatus.OK);
    }
}
