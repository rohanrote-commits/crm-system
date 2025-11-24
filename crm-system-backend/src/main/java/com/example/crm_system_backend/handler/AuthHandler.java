package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.UserSession;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.example.crm_system_backend.service.serviceImpl.UserSessionService;
import com.example.crm_system_backend.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class AuthHandler {

    private final UserSessionService userSessionService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthHandler(UserSessionService userSessionService, UserService userService, JwtUtil jwtUtil) {
        this.userSessionService = userSessionService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }


    public User signUpMasterAdmin(UserDTO request) {
        log.info("Request for signing up master admin is received, AuthHandler:signUpMasterAdmin");
        if (userService.checkUserByEmail(request.getEmail())) {
            log.error("Account already exists with email: {}", request.getEmail());
            throw new UserException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
        if (userService.checkUserByMobileNumber(request.getMobileNumber())) {
            log.error("Account already exists with mobile number: {}", request.getMobileNumber());
            throw new UserException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setRole(Roles.MASTER_ADMIN);
        user.setRegisteredOn(LocalDateTime.now());
        log.info("Request for signing up master admin is processed");
        return userService.registerUser(user);
    }

    public String loginRequest(UserDTO request) {
        log.info("Request for login is received in AuthHandler:loginRequest");
        if (userSessionService.findSessionByEmail(request.getEmail())) {
            userSessionService.deleteSessionByEmail(request.getEmail());
        }
        Optional<User> optionalUser = userService.getUser(request);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String token = jwtUtil.generateToken(user);
            UserSession userSession = new UserSession();
            userSession.setEmail(user.getEmail());
            userSession.setToken(token);
            userSessionService.saveSession(userSession);
            return token;
        } else {
            log.error("User not found with email: {}", request.getEmail());
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

    }

    public void logoutHandler(String email) {
        log.info("Request for logout is received in AuthHandler:logoutHandler");
        if (userSessionService.findSessionByEmail(email)) {
            userSessionService.deleteSessionByEmail(email);
        } else {
            log.error("User not found with email: {}", email);
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }
      log.info("Request for logout is processed");
    }
}
