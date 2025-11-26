package com.example.crm_system_backend.controller;


import com.example.crm_system_backend.annotations.RoleRequired;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.AuthHandler;
import com.example.crm_system_backend.UserHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/crm/user")
public class UserController {
    @Autowired
    private UserHandler userHandler;
    @Autowired
    private AuthHandler authHandler;

    /**
     * Registers a new user in the system. The user registration details are provided via
     * the request body, and the user performing the registration can optionally be set
     * based on the `userId` attribute from the HTTP request.
     *
     * @param userDTO the user data transfer object containing information about the user to be registered
     * @param request the HTTP request object containing details of the request, including the `userId` of the user performing the operation
     * @return a ResponseEntity containing the registered user's data and a status of {@code HttpStatus.CREATED}
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @PostMapping("/register")
    public ResponseEntity<UserDTO> user(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        Object registeredById = request.getAttribute("userId");
        if (registeredById != null) {
            userDTO.setRegisteredBy((Long) registeredById);
        }
        return new ResponseEntity<>(userHandler.save(userDTO), HttpStatus.CREATED);
    }

    /**
     * Retrieves a user by their ID. The user ID is extracted from the request's attributes.
     *
     * @param id      the ID of the user to be retrieved (not directly used in the method implementation)
     * @param request the HTTP servlet request containing the user ID as a request attribute
     * @return a ResponseEntity containing the user details and an HTTP status of OK
     */
    @GetMapping("/get-user")
    public ResponseEntity<User> getUserById(Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return new ResponseEntity<>(userHandler.getById(userId), HttpStatus.OK);
    }

    /**
     * Handles the registration process for a new Master Admin user. The registration details
     * are provided in the request body and include attributes such as name, email, and password.
     * This method ensures that the user does not already exist before proceeding with the creation.
     *
     * @param request a data transfer object (UserDTO) containing the registration details
     *                for the new user, such as email, password, and personal information
     * @return a ResponseEntity object with the created user details in the response body
     * and an HTTP status of {@code HttpStatus.CREATED}
     */
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody UserDTO request) {

        return new ResponseEntity<>(authHandler.signUpMasterAdmin(request), HttpStatus.CREATED);
    }

    /**
     *
     */
    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody UserDTO request) {
        return new ResponseEntity<>(authHandler.loginRequest(request), HttpStatus.OK);
    }

    /**
     * Retrieves a list of all user details accessible to the requester. The request
     * is authorized based on the role "MASTER_ADMIN".
     *
     * @param request the HTTP servlet request containing attributes like the `userId` of the requester
     * @return a ResponseEntity containing a list of UserDTOs representing user details and an HTTP status of OK
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("userId:{}", userId);
        return new ResponseEntity<>(userHandler.getUsers(userId), HttpStatus.OK);

    }

    /**
     * Handles the "forget password" functionality. This method receives a UserDTO object
     * in the request body containing the necessary information for processing the password
     * reset request, such as the user's email address. It delegates the operation to the
     * UserHandler to perform the reset.
     *
     * @param forgetPasswordDTO the data transfer object containing user information required
     *                          to initiate the password reset process (e.g., email)
     * @return a ResponseEntity containing the response from the UserHandler and an HTTP status of OK
     */
    @PostMapping("/forget")
    public ResponseEntity<?> forgetPassword(@RequestBody UserDTO forgetPasswordDTO) {
        return new ResponseEntity<>(userHandler.forgetPassword(forgetPasswordDTO), HttpStatus.OK);
    }

    /**
     * Updates the details of an existing user in the system. The user information to be
     * updated is specified in the request body, and the user to be updated is determined
     * based on the `userId` attribute in the HTTP servlet request.
     *
     * @param userDTO the data transfer object containing the updated details of the user
     * @param request the HTTP servlet request containing the `userId` attribute of the user being updated
     * @return a ResponseEntity containing the updated user details and an HTTP status of OK
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return new ResponseEntity<>(userHandler.edit(userId, userDTO), HttpStatus.OK);
    }

    /**
     * Updates the details of a sub-user in the system. The sub-user information to be updated
     * is provided in the request body, and the operation is authorized for roles "ADMIN" or "MASTER_ADMIN".
     * The user performing the update is determined from the `userId` attribute in the HTTP request.
     *
     * @param userDTO the data transfer object containing the updated details of the sub-user
     * @param request the HTTP servlet request containing the `userId` attribute of the user performing the operation
     * @return a ResponseEntity containing the updated sub-user details and an HTTP status of OK
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @PutMapping("/update-sub_user")
    ResponseEntity<?> updateSubUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return new ResponseEntity<>(userHandler.editSubUser(userId, userDTO), HttpStatus.OK);
    }

    /**
     * Deletes the user identified by the `userId` attribute from the current HTTP request.
     * The method retrieves the user ID, invokes the user deletion process, and returns
     * a response indicating successful deletion and logout.
     *
     * @param request the HTTP servlet request containing the `userId` attribute of the
     */
    @DeleteMapping("/delete-user")
    ResponseEntity<?> deleteUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userHandler.delete(userId);
        return new ResponseEntity<>("Deleted and Logged out Successfully ", HttpStatus.OK);
    }

    /**
     * Deletes a sub-user in the system. This operation is authorized for users with roles
     * "ADMIN" or "MASTER_ADMIN". The sub-user to be deleted is identified using details provided
     * in the request body, while the user performing the operation is identified using the
     * `userId` attribute from the HTTP request.
     *
     * @param userDTO the data transfer object containing information about the sub-user to be deleted
     * @param request the HTTP servlet request containing the `userId` attribute of the user performing the operation
     * @return a ResponseEntity with a message of successful deletion and an HTTP status of OK
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @DeleteMapping("/delete-sub_user")
    ResponseEntity<?> deleteSubUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userHandler.deleteSubUser(userId, userDTO);
        return new ResponseEntity<>("Deleted Successfully ", HttpStatus.OK);
    }

    /**
     * Handles the bulk upload of user data from a file. The uploaded file is processed and
     * stored in the system. This operation requires the user to have one of the roles
     * "ADMIN" or "MASTER_ADMIN".
     *
     * @param file    the multipart file containing user data for bulk upload
     * @param request the HTTP servlet request containing information such as the `userId`
     *                and `role` attributes of the user performing the operation
     * @return a ResponseEntity containing a success message and an HTTP status of OK
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @PostMapping("/upload-user-file")
    ResponseEntity<?> bulkUploadUserFile(@RequestParam MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        String s = userHandler.bulkUploadUser(file, userId);
        return new ResponseEntity<>(s, HttpStatus.OK);
    }

    /**
     * Logs out the user associated with the given email retrieved from the HTTP request.
     * This method invalidates the user's session or authentication details.
     *
     * @param request the HTTP servlet request containing attributes such as the user's email
     * @return a ResponseEntity containing a success message and an HTTP status of OK
     */
    @GetMapping("/logout")
    ResponseEntity<?> logout(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        authHandler.logoutHandler(email);
        return new ResponseEntity<>("Logged out Successfully", HttpStatus.OK);
    }

}
