package com.example.crm_system_backend.controller.view_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/crm")
public class AuthPageController {
    @GetMapping("/login")
    public String login(){
        return "users/login";
    }

    @GetMapping("/reset-password")
    public String resetPassword(){
        return "users/reset-password";
    }
    @GetMapping("/sign-up")
    public String signUp(){
        return "users/signup";
    }
}
