package com.example.crm_system_backend.controller.view_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/crm/leads")
public class LeadPageController {
    @GetMapping("/upload")
    public String upload(){
        return "leads/upload_lead";
    }
    @GetMapping("/view-error")
    public String error(){
        return "leads/view_error_lead";
    }

}
