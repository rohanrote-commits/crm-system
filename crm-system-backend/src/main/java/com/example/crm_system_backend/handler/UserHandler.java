package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.UserList;
import com.example.crm_system_backend.constants.FileTemplateType;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.helper.UserExcelHelper;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class UserHandler implements IHandler<UserDTO> {

    @Autowired
    private IUserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthHandler authHandler;
    @Autowired
    private UserExcelHelper userExcelHelper;

    @Autowired
    private UploadHistoryService uploadHistoryService;

    @Autowired
    private ErrorRecordHandler errorRecordHandler;

    /**
     * Saves a new user based on the provided {@code UserDTO}.
     * Validates the user's email and mobile number against the existing records to ensure uniqueness.
     * Ensures that the address details are consistent if provided.
     * Registers the user and copies the registered data back to the provided {@code UserDTO}.
     *
     * @param userDTO the user data transfer object containing the user's details to be saved
     * @return the saved user data transfer object enriched with the registered details
     * @throws UserException if the email or mobile number already exists, or if the address details are invalid
     */
    @Override
    public UserDTO save(UserDTO userDTO) {
        log.info("Enter : UserHandler:save");
        if (userService.checkUserByEmail(userDTO.getEmail())) {
            log.error("Email already exists: {}", userDTO.getEmail());
            throw new UserException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userService.checkUserByMobileNumber(userDTO.getMobileNumber())) {
            log.error("Exit: UserHandler:save with error: Email already exists: {}", userDTO.getEmail());
            throw new UserException(ErrorCode.MOBILE_NUMBER_ALREADY_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        if (user.getAddress() == null) {
            if (user.getCity() != null ||
                    user.getState() != null ||
                    user.getCountry() != null ||
                    user.getPinCode() != null) {
                log.error("Exit: UserHandler:save with error: Invalid address: " + user.getAddress());
                throw new UserException(ErrorCode.INVALID_ADDRESS);
            }
        }
        user.setRegisteredOn(java.time.LocalDateTime.now());
        BeanUtils.copyProperties(userService.registerUser(user), userDTO);
        log.info("Exit : UserHandler:save");
        return userDTO;
    }

    /**
     * Retrieves all user records in the form of a list of {@code UserDTO} objects.
     *
     * @return a list of {@code UserDTO} objects representing all users.
     * Returns an empty list if no users are found.
     */
    @Override
    public List<UserDTO> getAll() {
        return List.of();
    }

    /**
     * Retrieves a {@code User} entity by its unique identifier.
     * If the user is not found, it throws a {@code UserException} with the {@code ErrorCode.USER_NOT_FOUND} error code.
     *
     * @param id the unique identifier of the user to retrieve
     * @return the {@code User} entity corresponding to the given ID
     * @throws UserException if the user with the specified ID is not found
     */
    public User getById(Long id) {
        log.info("Request for getting user is in user Handler for user id" + id);
        return userService.getUserById(id).orElseThrow(() -> {
            log.error("User not found with id: " + id);
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        });
    }

    /**
     * Retrieves a list of users as a collection of {@code UserDTO} objects based on the given user ID.
     * This method first determines the role of the user identified by the provided ID to check
     * whether the user is a MASTER_ADMIN or ADMIN. Depending on the role, it fetches the users
     * either managed by a master admin, including their respective admins and their users, or
     * directly those under a single admin's management.
     *
     * @param id the unique identifier of the user whose managed users are to be retrieved
     * @return a list of {@code UserDTO} objects representing the users
     */
    public List<UserDTO> getUsers(Long id) {
        log.info("Enter : UserHandler:getUsers");
        List<User> users = new ArrayList<>();
        User accessingUser = userService.getUserById(id).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (userRepo.findRoleById(id) == Roles.MASTER_ADMIN) {
            log.info("Request for getting users is in user Handler for master admin");
            users = userService.getAllUserByMasterAdmin(id);
        } else {
            if (userRepo.findRoleById(id) == Roles.ADMIN) {
                users.add(userService.getUserById(accessingUser.getRegisteredBy()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND)));
                log.info("Request for getting users is in user Handler for admin");
                users = userService.getAllUsersByAdmin(id);
            } else if (userRepo.findRoleById(id) == Roles.BASIC) {
                users.add(accessingUser);
                User registeringUser = userService.getUserById(accessingUser.getRegisteredBy()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
                if (registeringUser.getRole() == Roles.MASTER_ADMIN) {
                    users.add(registeringUser);
                } else {
                    users.add(registeringUser);
                    users.add(userService.getUserById(userService.getUserById(accessingUser.getRegisteredBy()).get().getRegisteredBy()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND)));
                }
                //users.add(userService.getUserById(userService.getUserById(accessingUser.getRegisteredBy()).get().getRegisteredBy()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND)));

            }

        }
        log.info("Exit : UserHandler:getUsers");
        return users.stream().map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setEmailOfAdminRegistered(userRepo.findEmailById(user.getRegisteredBy()));
            BeanUtils.copyProperties(user, userDTO);
            return userDTO;
        }).toList();

    }

    /**
     * Updates the user's password if the provided email exists and the new password is different
     * from the existing password. Throws exceptions if the user is not found or the password
     * already exists.
     *
     * @param forgetPasswordDTO the {@code UserDTO} containing the user's email and the new password
     * @return the {@code UserDTO} with the updated password details
     * @throws UserException if the user does not exist or the provided password is already in use
     */
    public UserDTO forgetPassword(UserDTO forgetPasswordDTO) {
        log.info("Enter : UserHandler:forgetPassword");
        Optional<User> userOptional = userRepo.getUserByEmail(forgetPasswordDTO.getEmail());
        if (userOptional.isPresent()) {
            if (userOptional.get().getPassword().equals(forgetPasswordDTO.getPassword())) {
                log.error("Exit: UserHandler:forgetPassword with error: Password should not be same");
                throw new UserException(ErrorCode.PASSWORD_SHOULD_NOT_BE_SAME);
            }
            userOptional.get().setPassword(forgetPasswordDTO.getPassword());
            userService.registerUser(userOptional.get());
            log.info("Exit : UserHandler:forgetPassword");
            return forgetPasswordDTO;
        } else {
            log.error("Exit: UserHandler:forgetPassword with error: User not found");
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * Updates an existing user identified by the given ID with the provided {@code UserDTO} entity's details.
     * The method updates specific fields such as password, mobile number, address, city, state, country, and pin code
     * if they are provided in the {@code entity}.
     *
     * @param Id     the unique identifier of the user to be updated
     * @param entity the {@code UserDTO} containing the updated details for the user
     * @return the updated {@code UserDTO} reflecting the changes made
     */
    @Override
    public UserDTO edit(Long Id, UserDTO entity) {
        log.info("Enter : UserHandler:edit");
        Optional<User> userOptional = userService.getUserById(Id);
        if (userOptional.isPresent()) {
            if (entity.getPassword() != null) {
                userOptional.get().setPassword(entity.getPassword());
            }
            if (entity.getMobileNumber() != null) {
                userOptional.get().setMobileNumber(entity.getMobileNumber());
            }
        }
        if (entity.getAddress() != null) {
            userOptional.get().setAddress(entity.getAddress());
            userOptional.get().setCity(entity.getCity());
            userOptional.get().setState(entity.getState());
            userOptional.get().setCountry(entity.getCountry());
            userOptional.get().setPinCode(entity.getPinCode());
        }
        userService.registerUser(userOptional.get());
        log.info("Exit : UserHandler:edit");
        return entity;


    }

    /**
     * Edits a sub-user's information based on the given user ID and the updated details in {@code UserDTO}.
     * The method ensures that specific fields like mobile number, address, city, state, country, and pin code
     * are updated if they differ from the existing values. Throws appropriate exceptions if the user cannot
     * be found or the data is not updatable.
     *
     * @param id      the unique identifier of the user whose sub-user information is to be edited
     * @param userDTO the {@code UserDTO} containing the updated sub-user details
     * @return the updated {@code User} entity reflecting the changes made
     * @throws UserException if the sub-user cannot be found with the provided email,
     *                       or if no updatable fields are provided
     */
    public User editSubUser(Long id, UserDTO userDTO) {
        log.info("Enter : UserHandler:editSubUser");
        List<UserDTO> users = getUsers(id);
        UserDTO userDTO1 = users.stream().filter(user -> user.getEmail().equals(userDTO.getEmail()))
                .findFirst().orElseThrow(() -> new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL));
        Optional<User> user = userService.getUser(userDTO1);
        boolean flag = false;
        if (user.isPresent()) {


            if (!userDTO.getMobileNumber().isEmpty() && !userDTO.getMobileNumber().equals(user.get().getMobileNumber())) {
                user.get().setMobileNumber(userDTO.getMobileNumber());
                flag = true;
            }
            log.info("Mobile Number is " + userDTO.getMobileNumber());


            if (!userDTO.getAddress().isEmpty() && !userDTO.getAddress().equals(user.get().getAddress())) {
                flag = true;
                user.get().setAddress(userDTO.getAddress());
                user.get().setCity(userDTO.getCity());
                user.get().setState(userDTO.getState());
                user.get().setCountry(userDTO.getCountry());
                user.get().setPinCode(userDTO.getPinCode());
            }
            if (!flag) {
                log.error("Exit: UserHandler:editSubUser with error: User data not updatable");
                throw new UserException(ErrorCode.USER_DATA_NOT_UPDATABLE);
            }
            log.info("Exit : UserHandler:editSubUser");
            return userService.registerUser(user.get());
        } else {
            log.error("Exit: UserHandler:editSubUser with error: User not found");
            throw new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL);
        }

    }

    /**
     * Deletes a user and all associated subordinate users based on their role.
     * <p>
     * If the user with the given ID is a MASTER_ADMIN, deletes all users managed
     * by the MASTER_ADMIN, including subordinate admins and their respective users.
     * If the user is an ADMIN, deletes all users managed by the ADMIN.
     * Finally, deletes the user identified by the provided ID and ends
     * their active session.
     *
     * @param leadId the unique identifier of the user to be deleted
     */
    @Override
    public void delete(Long leadId) {
        log.info("Enter : UserHandler:delete");
        Optional<User> user = userService.getUserById(leadId);
        if (user.isPresent()) {
            if (user.get().getRole() == Roles.MASTER_ADMIN) {
                List<User> users = userService.getAllUserByMasterAdmin(leadId);
                users.stream().forEach(user1 -> {
                    userService.deleteUser(user1);
                });
            } else if (user.get().getRole() == Roles.ADMIN) {
                List<User> users = userService.getAllUsersByAdmin(leadId);
                users.stream().forEach(user1 -> {
                    userService.deleteUser(user1);
                });
            }
            userService.deleteUser(user.get());
            authHandler.logoutHandler(user.get().getEmail());
        }

        log.info("Exit : UserHandler:delete");

    }

    @Override
    public void bulkUpload(MultipartFile file, Long id) {

    }

    /**
     * Deletes a sub-user identified by the provided email from the list of users retrieved using the given ID.
     * <p>
     * The method first checks if a sub-user exists with the provided email in the list of users associated with
     * the given ID. If the sub-user is found, it retrieves the corresponding user entity and deletes it.
     * Throws a {@code UserException} if the sub-user with the provided email does not exist.
     *
     * @param id      the unique identifier of the user whose sub-user is to be deleted
     * @param userDTO the {@code UserDTO} object containing the email of the sub-user to be
     */
    public void deleteSubUser(Long id, UserDTO userDTO) {
        log.info("Enter : UserHandler:deleteSubUser");
        List<UserDTO> users = getUsers(id);
        UserDTO userDTO1 = users.stream().filter(user -> user.getEmail().equals(userDTO.getEmail()))
                .findFirst().orElseThrow(() -> new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL));
        Optional<User> user = userService.getUser(userDTO1);
        if (user.isPresent()) {
            userService.deleteUser(user.get());
        } else {
            log.error("Exit: UserHandler:deleteSubUser with error: User Not Found with email: " + userDTO.getEmail());
            throw new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL);
        }

    }

    /**
     * Handles the bulk upload of user data from an Excel file and processes it accordingly.
     * This method validates the file, processes the data, and records the upload history.
     * If there are any issues during the upload, exceptions are thrown, and errors are logged.
     *
     * @param file the {@code MultipartFile} containing user data to be uploaded
     * @param id   the unique identifier of the user initiating the upload
     * @throws UserException if the initiating user is not found or if the email of any user in the upload already exists
     * @throws LeadException if there is an error during file processing or if an exception occurs during upload
     */


    public String bulkUploadUser(MultipartFile file, Long id) {
        log.info("Enter : UserHandler:bulkUploadUser");
        UploadHistory uploadHistory = new UploadHistory();
        uploadHistory.setFileName(file.getOriginalFilename());
        uploadHistory.setUploadStatus(UploadStatus.PROCESSING);
        uploadHistory.setUploadedAt(LocalDateTime.now());

        User savedUser = userService.getUserById(id)
                .orElseThrow(() ->{
                    log.error("Exit: UserHandler:bulkUploadUser with error: User not found");
                    return new UserException(ErrorCode.USER_NOT_FOUND);});
        uploadHistory.setUploadedBy(savedUser.getEmail());
        uploadHistory.setFileTemplateType(FileTemplateType.USER);

        try {
            UserList userList = userExcelHelper.processExcelData(file, savedUser.getRole().name(), uploadHistory).get();

            // Process valid users

            for (User user : userList.getValidUserList()) {
                if (userRepo.existsByEmail(user.getEmail())) {
                    log.warn("Skipping duplicate user: {}", user.getEmail());
                    continue; // skip duplicates
                }
                user.setRegisteredBy(id);
                user.setRegisteredOn(LocalDateTime.now());
                userService.registerUser(user);
            }


            // Process invalid users
            if (!userList.getInvalidUserList().isEmpty()) {
                uploadHistory.setUploadStatus(UploadStatus.FAILED);
                UploadHistory savedUploadHistory = uploadHistoryService.save(uploadHistory);

                ErrorRecord errorRecord = new ErrorRecord();
                errorRecord.setUplodedBy(savedUser.getEmail());
                errorRecord.setUploadHistoryId(savedUploadHistory.getId());
                errorRecord.setErrorUserList(userList.getInvalidUserList());
                errorRecord.setFileName(file.getOriginalFilename());
                log.error("Invalid User List: {}", userList.getInvalidUserList());
                errorRecordHandler.saveErrorRecord(errorRecord);


            }

            if (userList.getValidUserList().isEmpty()) {
                uploadHistory.setUploadStatus(UploadStatus.FAILED);
            } else if (!userList.getInvalidUserList().isEmpty() && !userList.getValidUserList().isEmpty()) {
                uploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
            } else {
                uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
            }
            uploadHistoryService.save(uploadHistory);

            if (uploadHistory.getUploadStatus() == UploadStatus.SUCCESS) {
                return "All the Users are uploaded successfully";
            } else if (uploadHistory.getUploadStatus() == UploadStatus.PARTIALLY_SUCCESS) {
                return "Partially the Users are uploaded successfully";
            } else {
                return "The Users are not uploaded successfully";
            }


        } catch (Exception e) {
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            uploadHistoryService.save(uploadHistory);
           log.error("Exit: UserHandler:bulkUploadUser with error: " + e.getMessage());
            throw new UserException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }

    }

}


