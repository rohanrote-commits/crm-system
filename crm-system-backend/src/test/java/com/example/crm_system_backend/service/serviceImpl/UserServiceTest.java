package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepo userRepo;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setup() {
        encoder = new BCryptPasswordEncoder();
    }


    @Test
    void testRegisterUser_Success() {
        User user = new User();
        user.setPassword("raw123");

        when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.registerUser(user);

        assertNotNull(result);
        assertNotEquals("raw123", result.getPassword()); // Should be encoded
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        User user = new User();

        doNothing().when(userRepo).delete(user);

        userService.deleteUser(user);

        verify(userRepo, times(1)).delete(user);
    }

    @Test
    void testDeleteUser_WhenRepoThrowsException() {
        User user = new User();

        doThrow(new RuntimeException("Delete failed"))
                .when(userRepo).delete(user);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(user));

        assertEquals("Delete failed", ex.getMessage());
        verify(userRepo).delete(user);
    }


    @Test
    void testGetAllUsers_ShouldReturnEmptyList() {
        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    void testGetAllUsersByAdmin_Success() {
        User u1 = new User();
        User u2 = new User();

        when(userRepo.findUsersByRegisteredBy(10L))
                .thenReturn(List.of(u1, u2));

        List<User> list = userService.getAllUsersByAdmin(10L);

        assertEquals(2, list.size());
        verify(userRepo, times(1)).findUsersByRegisteredBy(10L);
    }


    @Test
    void testGetAllUserByMasterAdmin_RecursiveSuccess() {
        // Master admin (ID=1) has two users
        User admin = new User();
        admin.setId(100L);
        admin.setRole(Roles.MASTER_ADMIN);

        User normalUser = new User();
        normalUser.setId(101L);
        normalUser.setRole(Roles.USER);

        when(userRepo.findUsersByRegisteredBy(1L))
                .thenReturn(List.of(admin, normalUser));

        // Admin(100) registers 1 user
        User subUser = new User();
        subUser.setId(200L);
        subUser.setRole(Roles.USER);

        when(userRepo.findUsersByRegisteredBy(100L))
                .thenReturn(List.of(subUser));

        List<User> result = userService.getAllUserByMasterAdmin(1L);

        assertEquals(3, result.size());
        verify(userRepo, times(1)).findUsersByRegisteredBy(1L);
        verify(userRepo, times(1)).findUsersByRegisteredBy(100L);
    }

    @Test
    void testGetUser_UserNotFound() {
        UserDTO dto = new UserDTO();
        dto.setEmail("test@mail.com");

        when(userRepo.findUserByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class,
                () -> userService.getUser(dto));

        assertEquals(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL.getMessage(), ex.getMessage());
    }

    @Test
    void testGetUser_WrongPassword() {
        UserDTO dto = new UserDTO();
        dto.setEmail("test@mail.com");
        dto.setPassword("wrong");

        User user = new User();
        user.setPassword(encoder.encode("correct"));

        when(userRepo.findUserByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        UserException ex = assertThrows(UserException.class,
                () -> userService.getUser(dto));

        assertEquals(ErrorCode.WRONG_CREDENTIALS, ex.getMessage());
    }

    @Test
    void testGetUser_CorrectPassword() {
        UserDTO dto = new UserDTO();
        dto.setEmail("test@mail.com");
        dto.setPassword("123");

        User user = new User();
        user.setPassword(encoder.encode("123")); // encoded

        when(userRepo.findUserByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.getUser(dto);

        assertTrue(result.isPresent());
        verify(userRepo).findUserByEmail("test@mail.com");
    }

    @Test
    void testCheckUserByEmail_Exists() {
        when(userRepo.existsByEmail("a@a.com")).thenReturn(true);

        assertTrue(userService.checkUserByEmail("a@a.com"));
        verify(userRepo).existsByEmail("a@a.com");
    }

    @Test
    void testCheckUserByEmail_NotExists() {
        when(userRepo.existsByEmail("a@a.com")).thenReturn(false);

        assertFalse(userService.checkUserByEmail("a@a.com"));
    }

    @Test
    void testCheckUserByMobileNumber_Exists() {
        when(userRepo.existsByMobileNumber("999")).thenReturn(true);

        assertTrue(userService.checkUserByMobileNumber("999"));
        verify(userRepo).existsByMobileNumber("999");
    }

    @Test
    void testCheckUserByMobileNumber_NotExists() {
        when(userRepo.existsByMobileNumber("999")).thenReturn(false);

        assertFalse(userService.checkUserByMobileNumber("999"));
    }

    @Test
    void testGetUserById_Found() {
        User user = new User();
        user.setId(10L);

        when(userRepo.findById(10L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(10L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepo.findById(10L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(10L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserByEmail_Found() {
        User user = new User();

        when(userRepo.getUserByEmail("mail@test.com"))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("mail@test.com");

        assertTrue(result.isPresent());
        verify(userRepo).getUserByEmail("mail@test.com");
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepo.getUserByEmail("mail@test.com"))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByEmail("mail@test.com");

        assertFalse(result.isPresent());
    }


    @Test
    void testGetAllUsersRegisterById_Found() {
        User u1 = new User();
        User u2 = new User();

        when(userRepo.findUsersByRegisteredBy(5L))
                .thenReturn(List.of(u1, u2));

        Optional<List<User>> result = userService.getAllUsersRegisterById(5L);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void testGetAllUsersRegisterById_NullList() {
        when(userRepo.findUsersByRegisteredBy(5L)).thenReturn(null);

        Optional<List<User>> result = userService.getAllUsersRegisterById(5L);

        assertTrue(result.isEmpty());
    }
}
