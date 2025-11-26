package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.UserList;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.helper.UserExcelHelper;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Disabled
@SpringJUnitConfig(UserHandler.class)
class UserHandlerTest {

    @Autowired
    private UserHandler userHandler;

    @Mock
    private IUserRepo userRepo;

    @Mock
    private UserService userService;

    @Mock
    private UploadHistoryService uploadHistoryService;

    @Mock
    private UserExcelHelper userExcelHelper;

    @Test
    void testBulkUploadUser_Success() {
        Long userId = 1L;
        MockMultipartFile mockFile = new MockMultipartFile("file", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});
        User mockUser = new User();
        mockUser.setEmail("admin@example.com");
        mockUser.setRole(Roles.ADMIN);

        UploadHistory mockUploadHistory = new UploadHistory();
        mockUploadHistory.setUploadStatus(UploadStatus.SUCCESS);
        mockUploadHistory.setUploadedBy(mockUser.getEmail());

        List<User> validUsers = new ArrayList<>();
        User validUser = new User();
        validUser.setEmail("user1@example.com");
        validUsers.add(validUser);

        UserList mockUserList = new UserList();
        mockUserList.setValidUserList(validUsers);
        mockUserList.setInvalidUserList(new ArrayList<>());

        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));
       // when(userExcelHelper.processExcelData(mockFile, mockUser.getRole().name(), mockUploadHistory)).thenReturn(Optional.of(mockUserList));
        when(userRepo.existsByEmail(validUser.getEmail())).thenReturn(false);
        when(userService.registerUser(validUser)).thenReturn(validUser);

        String response = userHandler.bulkUploadUser(mockFile, userId);

        assertEquals("All the Users are uploaded successfully", response);

        verify(userService).registerUser(validUser);
        verify(uploadHistoryService, times(2)).save(any(UploadHistory.class));
    }

    @Test
    void testBulkUploadUser_PartiallySuccess() {
        Long userId = 2L;
        MockMultipartFile mockFile = new MockMultipartFile("file", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});
        User mockUser = new User();
        mockUser.setEmail("admin@example.com");
        mockUser.setRole(Roles.ADMIN);

        UploadHistory mockUploadHistory = new UploadHistory();
        mockUploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
        mockUploadHistory.setUploadedBy(mockUser.getEmail());

        List<User> validUsers = new ArrayList<>();
        User validUser1 = new User();
        validUser1.setEmail("valid1@example.com");
        validUsers.add(validUser1);

        List<User> invalidUsers = new ArrayList<>();
        User invalidUser = new User();
        invalidUser.setEmail("invalid@example.com");
        invalidUsers.add(invalidUser);

        UserList mockUserList = new UserList();
        mockUserList.setValidUserList(validUsers);
        mockUserList.setInvalidUserList(invalidUsers);

        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));
       // when(userExcelHelper.processExcelData(mockFile, mockUser.getRole().name(), mockUploadHistory)).thenReturn(Optional.of(mockUserList));
        when(userRepo.existsByEmail(validUser1.getEmail())).thenReturn(false);
        when(userService.registerUser(validUser1)).thenReturn(validUser1);

        String response = userHandler.bulkUploadUser(mockFile, userId);

        assertEquals("Partially the Users are uploaded successfully", response);

        verify(userService).registerUser(validUser1);
        verify(uploadHistoryService, times(2)).save(any(UploadHistory.class));
    }

    @Test
    void testBulkUploadUser_Failure() {
        Long userId = 3L;
        MockMultipartFile mockFile = new MockMultipartFile("file", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});
        User mockUser = new User();
        mockUser.setEmail("admin@example.com");
        mockUser.setRole(Roles.ADMIN);

        UploadHistory mockUploadHistory = new UploadHistory();
        mockUploadHistory.setUploadStatus(UploadStatus.FAILED);
        mockUploadHistory.setUploadedBy(mockUser.getEmail());

        UserList mockUserList = new UserList();
        mockUserList.setValidUserList(new ArrayList<>());
        mockUserList.setInvalidUserList(new ArrayList<>());

        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));
        //when(userExcelHelper.processExcelData(mockFile, mockUser.getRole().name(), mockUploadHistory)).thenReturn(Optional.of(mockUserList));

        String response = userHandler.bulkUploadUser(mockFile, userId);

        assertEquals("The Users are not uploaded successfully", response);

        verify(uploadHistoryService, times(2)).save(any(UploadHistory.class));
        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    void testBulkUploadUser_UserNotFound() {
        Long userId = 4L;
        MockMultipartFile mockFile = new MockMultipartFile("file", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});

        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () -> userHandler.bulkUploadUser(mockFile, userId));

       // assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userService, never()).registerUser(any(User.class));
        verify(uploadHistoryService, never()).save(any(UploadHistory.class));
    }

    @Test
    void testBulkUploadUser_FileProcessingException() {
        Long userId = 5L;
        MockMultipartFile mockFile = new MockMultipartFile("file", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});
        User mockUser = new User();
        mockUser.setEmail("admin@example.com");
        mockUser.setRole(Roles.ADMIN);

        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));
        when(userExcelHelper.processExcelData(eq(mockFile), eq(mockUser.getRole().name()), any(UploadHistory.class)))
                .thenThrow(new RuntimeException("Error processing file"));

        UserException exception = assertThrows(UserException.class, () -> userHandler.bulkUploadUser(mockFile, userId));

       // assertEquals(ErrorCode.FILE_PROCESSING_EXCEPTION, exception.getErrorCode());
        verify(uploadHistoryService, atLeastOnce()).save(any(UploadHistory.class));
    }
}