package com.example.crm_system_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/crm")
public class PageController {
    @GetMapping("/login")
    public String login(){
        return "users/login";
    }

    @GetMapping("users/bulk-upload")
    public String bulkUpload(){
        return "users/bulk-upload";
    }
    @GetMapping("users/reset-password")
    public String resetPassword(){
        return "users/reset-password";
    }
    @GetMapping("users/sign-up")
    public String signUp(){
        return "users/signup";
    }
    @GetMapping("users/dashboard")
    public String dashboard(){
        return "dashboard";
    }
    @GetMapping("users/view-error")
    public String error(){
        return "users/view_error_user";
    }
    @GetMapping("/user-dashboard")
    public String userDashboard(){
        return "users/user-dashboard";
    }
    @GetMapping("/user-template")
    public String userTemplate(){
        return "user-template";
    }

    @GetMapping("/delete-user")
    public String deleteUser(){
        return "delete-user";
    }

}
