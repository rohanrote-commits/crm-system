package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.SignUpRequest;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.UserSession;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.example.crm_system_backend.service.serviceImpl.UserSessionService;
import com.example.crm_system_backend.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AuthHandler {

    private final UserSessionService userSessionService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthHandler(UserSessionService userSessionService,UserService userService, JwtUtil jwtUtil) {
        this.userSessionService = userSessionService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }


    public User signUpMasterAdmin(UserDTO request) {
        if (userService.checkUserByEmail(request.getEmail())) {
            throw new UserException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
        if (userService.checkUserByMobileNumber(request.getMobileNumber())) {
            throw new UserException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setRole(Roles.MASTER_ADMIN);

        return userService.registerUser(user);
    }

    public String loginRequest(UserDTO request){
        if(userSessionService.findSessionByEmail(request.getEmail())){
            userSessionService.deleteSessionByEmail(request.getEmail());
        }
        Optional<User> optionalUser = userService.getUser(request);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            String token = jwtUtil.generateToken(user);
            UserSession userSession = new UserSession();
            userSession.setEmail(user.getEmail());
            userSession.setToken(token);
            userSessionService.saveSession(userSession);
            return token;
        }else{
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

    }
}
