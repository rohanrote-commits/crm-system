package com.example.crm_system_backend.controller;


import com.example.crm_system_backend.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crm/")
public class UserController {

    @PostMapping("/register-admin")
    public ResponseEntity<?> admin(){
        return null;
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> user(){
        return null;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(UserDTO userDTO){

        return null;
    }
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(){
        return null;
    }


}
