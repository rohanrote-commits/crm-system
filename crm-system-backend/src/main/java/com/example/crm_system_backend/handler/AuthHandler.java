package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.UserSession;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.example.crm_system_backend.service.serviceImpl.UserSessionService;
import com.example.crm_system_backend.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AuthHandler {

    private final UserSessionService userSessionService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Constructs an instance of AuthHandler with the specified dependencies.
     *
     * @param userSessionService the service responsible for managing user sessions
     * @param userService        the service responsible for managing user-related operations
     * @param jwtUtil            the utility class for handling JWT operations
     */
    public AuthHandler(UserSessionService userSessionService, UserService userService, JwtUtil jwtUtil) {
        this.userSessionService = userSessionService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user as a Master Admin by validating the input data and creating the user
     * if no conflicts are found with the email or mobile number.
     *
     * @param request the user details provided for registration, encapsulated in a {@link UserDTO}.
     *                Contains information such as email, mobile number, and other personal details.
     * @return the created {@link User} object after successful registration.
     * The user's role is set to MASTER_ADMIN, and the registration timestamp is included.
     * @throws UserException if an account with the given email or mobile number already exists.
     */
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
        user.setRegisteredOn(LocalDateTime.now());
        return userService.registerUser(user);
    }

    /**
     * Handles the login request for a user. Checks if an active session for the user exists,
     * removes the session if present, authenticates the user based on the provided credentials,
     * generates a JWT token upon successful authentication, and stores the session details.
     *
     * @param request the login credentials and details of the user encapsulated in {@link UserDTO}.
     *                Includes the user's email and password for authentication.
     * @return a JWT token as a {@link String} if the user is successfully authenticated.
     * @throws UserException if the user does not exist or the credentials are invalid.
     */
    public String loginRequest(UserDTO request) {
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
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

    }

    /**
     * Handles the logout process for a user by removing their active session.
     * If a session is found for the given email, the session is deleted.
     * If no session is found, a {@link UserException} with the error code USER_NOT_FOUND is thrown.
     *
     * @param email the email address of the user whose session is to be ended
     * @throws UserException if no active session is found for the provided email
     */
    public void logoutHandler(String email) {
        if (userSessionService.findSessionByEmail(email)) {
            userSessionService.deleteSessionByEmail(email);
        } else {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

    }
}
