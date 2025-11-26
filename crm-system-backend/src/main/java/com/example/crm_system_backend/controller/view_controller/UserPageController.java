package com.example.crm_system_backend.controller.view_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/crm/users")
public class UserPageController {
//    @GetMapping("/login")
//    public String login(){
//        return "users/login";
//    }

    @GetMapping("/bulk-upload")
    public String bulkUpload(){
        return "users/bulk-upload";
    }
//    @GetMapping("users/reset-password")
//    public String resetPassword(){
//        return "users/reset-password";
//    }
//    @GetMapping("users/sign-up")
//    public String signUp(){
//        return "users/signup";
//    }
    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
    }
    @GetMapping("/view-error")
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
