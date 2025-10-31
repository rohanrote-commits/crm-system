package com.example.crm_system_backend.controller;


import com.example.crm_system_backend.dto.LoginRequest;
import com.example.crm_system_backend.dto.SignUpRequest;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.handler.AuthHandler;
import com.example.crm_system_backend.handler.IHandler;
import com.example.crm_system_backend.handler.UserHandler;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Handler;

@RestController
@RequestMapping("/crm/")
public class UserController {
    @Autowired
    private UserHandler userHandler;
    @Autowired
    private AuthHandler authHandler;

    @PostMapping("master/register-admin")
    public ResponseEntity<?> admin(UserDTO request){
        request.setRole(Roles.ADMIN);

        return null;
    }

    @PostMapping("admin/register-user")
    public ResponseEntity<?> user(UserDTO request){
        request.setRole(Roles.USER);

        return null;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(UserDTO request){

        return new ResponseEntity<>(authHandler.signUpMasterAdmin(request), HttpStatus.CREATED);
    }
    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(UserDTO request){
        return new ResponseEntity<>(authHandler.loginRequest(request), HttpStatus.OK);
    }


}
