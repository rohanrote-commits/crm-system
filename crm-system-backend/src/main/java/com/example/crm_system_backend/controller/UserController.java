package com.example.crm_system_backend.controller;


import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.handler.AuthHandler;
import com.example.crm_system_backend.handler.UserHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/forget")
    public ResponseEntity<?> forgetPassword(@RequestBody UserDTO forgetPasswordDTO){
     return new ResponseEntity<>(userHandler.forgetPassword(forgetPasswordDTO), HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return new ResponseEntity<>(userHandler.edit(userId,userDTO), HttpStatus.OK);
    }

    @PostMapping("/update-sub_user")
    ResponseEntity<?> updateSubUser(@RequestBody UserDTO userDTO, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
     return new ResponseEntity<>(userHandler.editSubUser(userId,userDTO), HttpStatus.OK);
    }

    @DeleteMapping("/delete-user")
    ResponseEntity<?> deleteUser(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return new ResponseEntity<>("Deleted and Logged out Successfully ", HttpStatus.OK);
    }

    @DeleteMapping("/delete-sub_user")
    ResponseEntity<?> deleteSubUser(UserDTO userDTO,HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        userHandler.deleteSubUser(userId,userDTO);
        return new ResponseEntity<>("Deleted Successfully ", HttpStatus.OK);
    }

    @GetMapping("/logout")
    ResponseEntity<?> logout(HttpServletRequest request){
        String email = (String) request.getAttribute("email");
        authHandler.logoutHandler(email);
        return new ResponseEntity<>("Logged out Successfully", HttpStatus.OK);
    }

}
