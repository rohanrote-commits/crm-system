package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.UserList;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.FileTemplateType;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.handler.AuthHandler;
import com.example.crm_system_backend.handler.ErrorRecordHandler;
import com.example.crm_system_backend.handler.UserHandler;
import com.example.crm_system_backend.helper.UserExcelHelper;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserHandler Test Suite")
class UserHandlerTest {

    @Mock
    private IUserRepo userRepo;

    @Mock
    private UserService userService;

    @Mock
    private AuthHandler authHandler;

    @Mock
    private UserExcelHelper userExcelHelper;

    @Mock
    private UploadHistoryService uploadHistoryService;

    @Mock
    private ErrorRecordHandler errorRecordHandler;

    @InjectMocks
    private UserHandler userHandler;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setEmail("testuser@example.com");
        userDTO.setMobileNumber("9876543210");
        userDTO.setPassword("password123");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setRole(Roles.BASIC);

        user = new User();
        user.setId(1L);
        user.setEmail("testuser@example.com");
        user.setMobileNumber("9876543210");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Roles.BASIC);
        user.setRegisteredOn(LocalDateTime.now());
    }

    // ==================== SAVE METHOD TESTS ====================
    @Nested
    @DisplayName("Save User Tests")
    class SaveUserTests {

        @Test
        @DisplayName("Should save a new user successfully with all valid details")
        void testSaveUserSuccess() {
            // Arrange
            when(userService.checkUserByEmail(userDTO.getEmail())).thenReturn(false);
            when(userService.checkUserByMobileNumber(userDTO.getMobileNumber())).thenReturn(false);
            when(userService.registerUser(any(User.class))).thenReturn(user);

            // Act
            UserDTO result = userHandler.save(userDTO);

            // Assert
            assertNotNull(result);
            assertEquals("testuser@example.com", result.getEmail());
            verify(userService, times(1)).checkUserByEmail(userDTO.getEmail());
            verify(userService, times(1)).checkUserByMobileNumber(userDTO.getMobileNumber());
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserException when email already exists")
        void testSaveUserWithExistingEmail() {
            // Arrange
            when(userService.checkUserByEmail(userDTO.getEmail())).thenReturn(true);

            // Act & Assert
            UserException exception = assertThrows(UserException.class, () -> {
                userHandler.save(userDTO);
            });

            assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), exception.getMessage());
            verify(userService, times(1)).checkUserByEmail(userDTO.getEmail());
            verify(userService, never()).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserException when mobile number already exists")
        void testSaveUserWithExistingMobileNumber() {
            // Arrange
            when(userService.checkUserByEmail(userDTO.getEmail())).thenReturn(false);
            when(userService.checkUserByMobileNumber(userDTO.getMobileNumber())).thenReturn(true);

            // Act & Assert
            UserException exception = assertThrows(UserException.class, () -> {
                userHandler.save(userDTO);
            });

            assertEquals(ErrorCode.MOBILE_NUMBER_ALREADY_EXISTS.getMessage(), exception.getMessage());
            verify(userService, never()).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserException when address is null but other address fields are provided")
        void testSaveUserWithInvalidAddress() {
            // Arrange
            userDTO.setAddress(null);
            userDTO.setCity("New York");
            userDTO.setState("NY");
            userDTO.setCountry("USA");
            userDTO.setPinCode("10001");

            when(userService.checkUserByEmail(userDTO.getEmail())).thenReturn(false);
            when(userService.checkUserByMobileNumber(userDTO.getMobileNumber())).thenReturn(false);

            // Act & Assert
            UserException exception = assertThrows(UserException.class, () -> {
                userHandler.save(userDTO);
            });

            assertEquals(ErrorCode.INVALID_ADDRESS.getMessage(), exception.getMessage());
            verify(userService, never()).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should save user when address is null and all address fields are null")
        void testSaveUserWithNullAddressAndNullAddressFields() {
            // Arrange
            userDTO.setAddress(null);
            userDTO.setCity(null);
            userDTO.setState(null);
            userDTO.setCountry(null);
            userDTO.setPinCode(null);

            when(userService.checkUserByEmail(userDTO.getEmail())).thenReturn(false);
            when(userService.checkUserByMobileNumber(userDTO.getMobileNumber())).thenReturn(false);
            when(userService.registerUser(any(User.class))).thenReturn(user);

            // Act
            UserDTO result = userHandler.save(userDTO);

            // Assert
            assertNotNull(result);
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should save user when complete address details are provided")
        void testSaveUserWithCompleteAddress() {
            // Arrange
            userDTO.setAddress("123 Main St");
            userDTO.setCity("New York");
            userDTO.setState("NY");
            userDTO.setCountry("USA");
            userDTO.setPinCode("10001");

            when(userService.checkUserByEmail(userDTO.getEmail())).thenReturn(false);
            when(userService.checkUserByMobileNumber(userDTO.getMobileNumber())).thenReturn(false);
            when(userService.registerUser(any(User.class))).thenReturn(user);

            // Act
            UserDTO result = userHandler.save(userDTO);

            // Assert
            assertNotNull(result);
            verify(userService, times(1)).registerUser(any(User.class));
        }
    }

    // ==================== GET ALL TESTS ====================
    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return empty list when no users exist")
        void testGetAllUsersReturnsEmptyList() {
            // Act
            List<UserDTO> result = userHandler.getAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== GET BY ID TESTS ====================
    @Nested
    @DisplayName("Get User By ID Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return user when user exists with given ID")
        void testGetByIdSuccess() {
            // Arrange
            when(userService.getUserById(1L)).thenReturn(Optional.of(user));

            // Act
            User result = userHandler.getById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser@example.com", result.getEmail());
            verify(userService, times(1)).getUserById(1L);
        }

        @Test
        @DisplayName("Should throw UserException when user does not exist")
        void testGetByIdUserNotFound() {
            // Arrange
            when(userService.getUserById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            UserException exception = assertThrows(UserException.class, () -> {
                userHandler.getById(999L);
            });

            assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
            verify(userService, times(1)).getUserById(999L);
        }
    }

    // ==================== GET USERS TESTS ====================
    @Nested
    @DisplayName("Get Users Tests")
    class GetUsersTests {

        @Test
        @DisplayName("Should return all users managed by MASTER_ADMIN")
        void testGetUsersByMasterAdmin() {
            // Arrange
            User masterAdmin = new User();
            masterAdmin.setId(1L);
            masterAdmin.setEmail("masteradmin@example.com");
            masterAdmin.setRole(Roles.MASTER_ADMIN);

            List<User> allUsers = new ArrayList<>();
            allUsers.add(masterAdmin);
            allUsers.add(user);

            when(userService.getUserById(1L)).thenReturn(Optional.of(masterAdmin));
            when(userRepo.findRoleById(1L)).thenReturn(Roles.MASTER_ADMIN);
            when(userService.getAllUserByMasterAdmin(1L)).thenReturn(allUsers);
            when(userRepo.findEmailById(anyLong())).thenReturn("masteradmin@example.com");

            // Act
            List<UserDTO> result = userHandler.getUsers(1L);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userService, times(1)).getAllUserByMasterAdmin(1L);
        }

        @Test
        @DisplayName("Should return users managed by ADMIN")
        void testGetUsersByAdmin() {
            // Arrange
            User admin = new User();
            admin.setId(2L);
            admin.setEmail("admin@example.com");
            admin.setRole(Roles.ADMIN);
            admin.setRegisteredBy(1L);

            User masterAdmin = new User();
            masterAdmin.setId(1L);
            masterAdmin.setRole(Roles.MASTER_ADMIN);

            List<User> adminUsers = new ArrayList<>();
            adminUsers.add(masterAdmin);
            adminUsers.add(user);

            when(userService.getUserById(2L)).thenReturn(Optional.of(admin));
            when(userRepo.findRoleById(2L)).thenReturn(Roles.ADMIN);
            when(userService.getUserById(1L)).thenReturn(Optional.of(masterAdmin));
            when(userService.getAllUsersByAdmin(2L)).thenReturn(List.of(user));
            when(userRepo.findEmailById(anyLong())).thenReturn("admin@example.com");

            // Act
            List<UserDTO> result = userHandler.getUsers(2L);

            // Assert
            assertNotNull(result);
            assertTrue(result.size() > 0);
            verify(userService, times(1)).getAllUsersByAdmin(2L);
        }

        @Test
        @DisplayName("Should return BASIC user with registering admin when registering user is MASTER_ADMIN")
        void testGetUsersByBasicUserWithMasterAdminRegistrar() {
            // Arrange
            User basicUser = new User();
            basicUser.setId(3L);
            basicUser.setEmail("basicuser@example.com");
            basicUser.setRole(Roles.BASIC);
            basicUser.setRegisteredBy(1L);

            User masterAdmin = new User();
            masterAdmin.setId(1L);
            masterAdmin.setEmail("masteradmin@example.com");
            masterAdmin.setRole(Roles.MASTER_ADMIN);

            when(userService.getUserById(3L)).thenReturn(Optional.of(basicUser));
            when(userRepo.findRoleById(3L)).thenReturn(Roles.BASIC);
            when(userService.getUserById(1L)).thenReturn(Optional.of(masterAdmin));
            when(userRepo.findEmailById(anyLong())).thenReturn("masteradmin@example.com");

            // Act
            List<UserDTO> result = userHandler.getUsers(3L);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size()); // basicUser and masterAdmin
            verify(userService, times(2)).getUserById(anyLong());
        }

        @Test
        @DisplayName("Should throw UserException when user not found")
        void testGetUsersUserNotFound() {
            // Arrange
            when(userService.getUserById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            UserException exception = assertThrows(UserException.class, () -> {
                userHandler.getUsers(999L);
            });

            assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        }
    }

    // ==================== FORGET PASSWORD TESTS ====================
    @Nested
    @DisplayName("Forget Password Tests")
    class ForgetPasswordTests {

        @Test
        @DisplayName("Should update password successfully when user exists")
        void testForgetPasswordSuccess() {
            // Arrange
            UserDTO forgetPasswordDTO = new UserDTO();
            forgetPasswordDTO.setEmail("testuser@example.com");
            forgetPasswordDTO.setPassword("newpassword123");

            User existingUser = new User();
            existingUser.setId(1L);
            existingUser.setEmail("testuser@example.com");
            existingUser.setPassword("oldpassword123");

            when(userRepo.getUserByEmail("testuser@example.com")).thenReturn(Optional.of(existingUser));
            when(userService.registerUser(any(User.class))).thenReturn(existingUser);

            // Act
            UserDTO result = userHandler.forgetPassword(forgetPasswordDTO);

            // Assert
            assertNotNull(result);
            assertEquals("testuser@example.com", result.getEmail());
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserException when new password is same as old password")
        void testForgetPasswordSameAsOld() {
            // Arrange
            UserDTO forgetPasswordDTO = new UserDTO();
            forgetPasswordDTO.setEmail("testuser@example.com");
            forgetPasswordDTO.setPassword("samepassword123");

            User existingUser = new User();
            existingUser.setId(1L);
            existingUser.setEmail("testuser@example.com");
            existingUser.setPassword("samepassword123");

            when(userRepo.getUserByEmail("testuser@example.com")).thenReturn(Optional.of(existingUser));

            // Act & Assert
            UserException exception = assertThrows(UserException.class, () -> {
                userHandler.forgetPassword(forgetPasswordDTO);
            });

            assertEquals(ErrorCode.PASSWORD_SHOULD_NOT_BE_SAME.getMessage(), exception.getMessage());
            verify(userService, never()).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserException when user not found with email")
        void testForgetPasswordUserNotFound() {
            // Arrange
            UserDTO forgetPasswordDTO = new UserDTO();
            forgetPasswordDTO.setEmail("nonexistent@example.com");
            forgetPasswordDTO.setPassword("newpassword123");

            when(userRepo.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            UserException exception = assertThrows(UserException.class, () -> {
                userHandler.forgetPassword(forgetPasswordDTO);
            });

            assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
            verify(userService, never()).registerUser(any(User.class));
        }
    }

    // ==================== EDIT USER TESTS ====================
    @Nested
    @DisplayName("Edit User Tests")
    class EditUserTests {

        @Test
        @DisplayName("Should update password when provided")
        void testEditUserPasswordUpdate() {
            // Arrange
            UserDTO updateDTO = new UserDTO();
            updateDTO.setPassword("newpassword123");

            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
            when(userService.registerUser(any(User.class))).thenReturn(user);

            // Act
            UserDTO result = userHandler.edit(1L, updateDTO);

            // Assert
            assertNotNull(result);
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should update mobile number when provided")
        void testEditUserMobileNumberUpdate() {
            // Arrange
            UserDTO updateDTO = new UserDTO();
            updateDTO.setMobileNumber("9876543211");

            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
            when(userService.registerUser(any(User.class))).thenReturn(user);

            // Act
            UserDTO result = userHandler.edit(1L, updateDTO);

            // Assert
            assertNotNull(result);
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should update complete address details when provided")
        void testEditUserAddressUpdate() {
            // Arrange
            UserDTO updateDTO = new UserDTO();
            updateDTO.setAddress("456 Oak Ave");
            updateDTO.setCity("Los Angeles");
            updateDTO.setState("CA");
            updateDTO.setCountry("USA");
            updateDTO.setPinCode("90001");

            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
            when(userService.registerUser(any(User.class))).thenReturn(user);

            // Act
            UserDTO result = userHandler.edit(1L, updateDTO);

            // Assert
            assertNotNull(result);
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should update all fields when multiple fields provided")
        void testEditUserMultipleFieldsUpdate() {
            // Arrange
            UserDTO updateDTO = new UserDTO();
            updateDTO.setPassword("newpassword123");
            updateDTO.setMobileNumber("9876543211");
            updateDTO.setAddress("456 Oak Ave");
            updateDTO.setCity("Los Angeles");
            updateDTO.setState("CA");
            updateDTO.setCountry("USA");
            updateDTO.setPinCode("90001");

            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
            when(userService.registerUser(any(User.class))).thenReturn(user);

            // Act
            UserDTO result = userHandler.edit(1L, updateDTO);

            // Assert
            assertNotNull(result);
            verify(userService, times(1)).registerUser(any(User.class));
        }
    }

    // ==================== EDIT SUB USER TESTS ====================
//    @Nested
//    @DisplayName("Edit Sub User Tests")
//    class EditSubUserTests {
//
//        @Test
//        @DisplayName("Should update sub-user mobile number successfully")
//        void testEditSubUserMobileNumberSuccess() {
//            // Arrange
//
//            UserDTO subUserDTO = new UserDTO();
//            subUserDTO.setEmail("subuser@example.com");
//            subUserDTO.setMobileNumber("9876543211");
//
//            User subUser = new User();
//            subUser.setId(2L);
//            subUser.setEmail("subuser@example.com");
//            subUser.setMobileNumber("9876543210");
//
//            List<UserDTO> usersList = new ArrayList<>();
//            UserDTO userDTOInList = new UserDTO();
//            userDTOInList.setEmail("subuser@example.com");
//            usersList.add(userDTOInList);
//
//            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
//            when(userRepo.findRoleById(1L)).thenReturn(Roles.ADMIN);
//            when(userService.getAllUsersByAdmin(1L)).thenReturn(List.of(subUser));
//            when(userService.getUserByEmail("subuser@example.com")).thenReturn(Optional.of(subUser));
//            when(userRepo.findEmailById(anyLong())).thenReturn("admin@example.com");
//            when(userService.getUserByEmail("subuser@example.com")).thenReturn(Optional.of(subUser));
//            when(userService.registerUser(any(User.class))).thenReturn(subUser);
//
//            // Act
//            User result = userHandler.editSubUser(1L, subUserDTO);
//
//            // Assert
//            assertNotNull(result);
//            verify(userService, times(1)).registerUser(any(User.class));
//        }
//
//        @Test
//        @DisplayName("Should update sub-user address successfully")
//        void testEditSubUserAddressSuccess() {
//            // Arrange
//            UserDTO subUserDTO = new UserDTO();
//            subUserDTO.setEmail("subuser@example.com");
//            subUserDTO.setMobileNumber("");
//            subUserDTO.setAddress("New Address");
//            subUserDTO.setCity("New City");
//            subUserDTO.setState("New State");
//            subUserDTO.setCountry("New Country");
//            subUserDTO.setPinCode("12345");
//
//            User subUser = new User();
//            subUser.setId(2L);
//            subUser.setEmail("subuser@example.com");
//            subUser.setAddress("Old Address");
//            subUser.setRegisteredBy(user.getId());
//
//            List<UserDTO> usersList = new ArrayList<>();
//            UserDTO userDTOInList = new UserDTO();
//            userDTOInList.setEmail("subuser@example.com");
//            usersList.add(userDTOInList);
//
//            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
//            when(userRepo.findRoleById(1L)).thenReturn(Roles.ADMIN);
//            when(userService.getAllUsersByAdmin(1L)).thenReturn(List.of(subUser));
//            when(userRepo.findEmailById(anyLong())).thenReturn("admin@example.com");
//            when(userService.getUserByEmail("subuser@example.com")).thenReturn(Optional.of(subUser));
//            when(userService.registerUser(any(User.class))).thenReturn(subUser);
//
//            // Act
//            User result = userHandler.editSubUser(1L, subUserDTO);
//
//            // Assert
//            assertNotNull(result);
//            verify(userService, times(1)).registerUser(any(User.class));
//        }
//
//        @Test
//        @DisplayName("Should throw UserException when no updatable fields provided")
//        void testEditSubUserNoUpdatableFields() {
//            // Arrange
//            UserDTO subUserDTO = new UserDTO();
//            subUserDTO.setEmail("subuser@example.com");
//            subUserDTO.setMobileNumber("");
//            subUserDTO.setAddress("");
//
//            User subUser = new User();
//            subUser.setId(2L);
//            subUser.setEmail("subuser@example.com");
//            subUser.setMobileNumber("9876543210");
//
//            List<UserDTO> usersList = new ArrayList<>();
//            UserDTO userDTOInList = new UserDTO();
//            userDTOInList.setEmail("subuser@example.com");
//            usersList.add(userDTOInList);
//
//            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
//            when(userRepo.findRoleById(1L)).thenReturn(Roles.ADMIN);
//            when(userService.getAllUsersByAdmin(1L)).thenReturn(List.of(subUser));
//            when(userRepo.findEmailById(anyLong())).thenReturn("admin@example.com");
//            when(userService.getUserByEmail("subuser@example.com")).thenReturn(Optional.of(subUser));
//
//            // Act & Assert
//            UserException exception = assertThrows(UserException.class, () -> {
//                userHandler.editSubUser(1L, subUserDTO);
//            });
//
//            assertEquals(ErrorCode.USER_DATA_NOT_UPDATABLE.getMessage(), exception.getMessage());
//            verify(userService, never()).registerUser(any(User.class));
//        }
//
//        @Test
//        @DisplayName("Should throw UserException when sub-user not found with email")
//        void testEditSubUserNotFound() {
//            // Arrange
//            UserDTO subUserDTO = new UserDTO();
//            subUserDTO.setEmail("nonexistent@example.com");
//
//            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
//            when(userRepo.findRoleById(1L)).thenReturn(Roles.ADMIN);
//            when(userService.getAllUsersByAdmin(1L)).thenReturn(new ArrayList<>());
//            when(userRepo.findEmailById(anyLong())).thenReturn("admin@example.com");
//
//            // Act & Assert
//            UserException exception = assertThrows(UserException.class, () -> {
//                userHandler.editSubUser(1L, subUserDTO);
//            });
//
//            assertEquals(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL, exception.getMessage());
//        }
//    }

    // ==================== DELETE USER TESTS ====================
    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete MASTER_ADMIN and all subordinate users")
        void testDeleteMasterAdminDeletesAll() {
            // Arrange
            User masterAdmin = new User();
            masterAdmin.setId(1L);
            masterAdmin.setEmail("masteradmin@example.com");
            masterAdmin.setRole(Roles.MASTER_ADMIN);

            List<User> allUsers = new ArrayList<>();
            allUsers.add(user);

            when(userService.getUserById(1L)).thenReturn(Optional.of(masterAdmin));
            when(userService.getAllUserByMasterAdmin(1L)).thenReturn(allUsers);

            // Act
            userHandler.delete(1L);

            // Assert
            verify(userService, times(1)).getAllUserByMasterAdmin(1L);
            verify(userService, times(2)).deleteUser(any(User.class)); // all users + master admin
            verify(authHandler, times(1)).logoutHandler("masteradmin@example.com");
        }

        @Test
        @DisplayName("Should delete ADMIN and all managed users")
        void testDeleteAdminDeletesManagedUsers() {
            // Arrange
            User admin = new User();
            admin.setId(2L);
            admin.setEmail("admin@example.com");
            admin.setRole(Roles.ADMIN);

            List<User> managedUsers = new ArrayList<>();
            managedUsers.add(user);

            when(userService.getUserById(2L)).thenReturn(Optional.of(admin));
            when(userService.getAllUsersByAdmin(2L)).thenReturn(managedUsers);

            // Act
            userHandler.delete(2L);

            // Assert
            verify(userService, times(1)).getAllUsersByAdmin(2L);
            verify(userService, times(2)).deleteUser(any(User.class)); // managed user + admin
            verify(authHandler, times(1)).logoutHandler("admin@example.com");
        }

        @Test
        @DisplayName("Should delete BASIC user only")
        void testDeleteBasicUserDeletesOnlyUser() {
            // Arrange
            when(userService.getUserById(1L)).thenReturn(Optional.of(user));

            // Act
            userHandler.delete(1L);

            // Assert
            verify(userService, times(1)).deleteUser(user);
            verify(authHandler, times(1)).logoutHandler("testuser@example.com");
        }
    }

    // ==================== DELETE SUB USER TESTS ====================
//    @Nested
//    @DisplayName("Delete Sub User Tests")
//    class DeleteSubUserTests {
//
//        @Test
//        @DisplayName("Should delete sub-user successfully")
//        void testDeleteSubUserSuccess() {
//            // Arrange
//            UserDTO subUserDTO = new UserDTO();
//            subUserDTO.setEmail("subuser@example.com");
//
//            User subUser = new User();
//            subUser.setId(2L);
//            subUser.setEmail("subuser@example.com");
//
//            List<UserDTO> usersList = new ArrayList<>();
//            UserDTO userDTOInList = new UserDTO();
//            userDTOInList.setEmail("subuser@example.com");
//            usersList.add(userDTOInList);
//
//            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
//            when(userRepo.findRoleById(1L)).thenReturn(Roles.ADMIN);
//            when(userService.getAllUsersByAdmin(1L)).thenReturn(List.of(subUser));
//            when(userRepo.findEmailById(anyLong())).thenReturn("admin@example.com");
//            when(userService.getUserByEmail("subuser@example.com")).thenReturn(Optional.of(subUser));
//
//            // Act
//            userHandler.deleteSubUser(1L, subUserDTO);
//
//            // Assert
//            verify(userService, times(1)).deleteUser(subUser);
//        }
//
//        @Test
//        @DisplayName("Should throw UserException when sub-user not found")
//        void testDeleteSubUserNotFound() {
//            // Arrange
//            UserDTO subUserDTO = new UserDTO();
//            subUserDTO.setEmail("nonexistent@example.com");
//
//            when(userService.getUserById(1L)).thenReturn(Optional.of(user));
//            when(userRepo.findRoleById(1L)).thenReturn(Roles.ADMIN);
//            when(userService.getAllUsersByAdmin(1L)).thenReturn(new ArrayList<>());
//            when(userRepo.findEmailById(anyLong())).thenReturn("admin@example.com");
//
//            // Act & Assert
//            UserException exception = assertThrows(UserException.class, () -> {
//                userHandler.deleteSubUser(1L, subUserDTO);
//            });
//
//            assertEquals(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL.getMessage(), exception.getMessage());
//            verify(userService, never()).deleteUser(any(User.class));
//        }
//    }

}