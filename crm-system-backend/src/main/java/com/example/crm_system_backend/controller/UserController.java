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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Handler;

@Slf4j
@RestController
@RequestMapping("/crm/user")
public class UserController {
    @Autowired
    private UserHandler userHandler;
    @Autowired
    private AuthHandler authHandler;



    @PostMapping("/register")
    public ResponseEntity<UserDTO> user(@RequestBody UserDTO userDTO,HttpServletRequest request){
        Object registeredById = request.getAttribute("userId");
        if (registeredById != null) {
            userDTO.setRegisteredBy((Long) registeredById);
        }
        return new ResponseEntity<>(userHandler.save(userDTO), HttpStatus.CREATED);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody UserDTO request){

        return new ResponseEntity<>(authHandler.signUpMasterAdmin(request), HttpStatus.CREATED);
    }
    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody UserDTO request){
        return new ResponseEntity<>(authHandler.loginRequest(request), HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        log.info("userId:{}",userId);
        return new ResponseEntity<>(userHandler.getUsers(userId), HttpStatus.OK);

    }


}
