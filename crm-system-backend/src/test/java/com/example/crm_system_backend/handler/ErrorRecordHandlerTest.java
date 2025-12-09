package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.InvalidUserError;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorRecordException;
import com.example.crm_system_backend.exception.UploadHistoryException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErrorRecordHandlerTest {

    private UploadHistoryService uploadHistoryService;
    private LeadService leadService;
    private ModelMapper mapper;
    private UserService userService;
    private IUserRepo userRepo;
    private ObjectMapper objectMapper;
    private ErrorRecordHandler handler;

    @BeforeEach
    void setUp() {
        uploadHistoryService = mock(UploadHistoryService.class);
        leadService = mock(LeadService.class);
        mapper = new ModelMapper();
        userService = mock(UserService.class);
        userRepo = mock(IUserRepo.class);
        objectMapper = new ObjectMapper();
        handler = new ErrorRecordHandler(uploadHistoryService, leadService, mapper, userService, userRepo, objectMapper);
    }

    private UploadHistory getUploadHistoryWithErrors() throws Exception {
        UploadHistory history = new UploadHistory();
        history.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
        history.setInvalidRecords(1);
        history.setValidRecords(0);
        InvalidLeadError invalidLeadError = new InvalidLeadError();
        invalidLeadError.setRowNumber(1);
        invalidLeadError.setLead(new Lead());
        invalidLeadError.setErrors(new HashMap<>());
        history.setErrorRecord(objectMapper.writeValueAsString(
                List.of(invalidLeadError))
        );
        history.setUpdatedAt(LocalDateTime.now());
        return history;
    }


    @Test
    void testFindErrorRecordByUploadHistoryId() throws Exception {
        UploadHistory history = getUploadHistoryWithErrors();
        when(uploadHistoryService.findById("1")).thenReturn(history);

        List<InvalidLeadError> response = handler.findErrorRecordByUploadHistoryId("1");

        assertEquals(1, response.size());
        verify(uploadHistoryService).findById("1");
    }

    @Test
    void testFindErrorRecordByUploadHistoryId_NoRecords() {
        UploadHistory history = new UploadHistory();
        when(uploadHistoryService.findById("1")).thenReturn(history);

        assertThrows(UploadHistoryException.class,
                () -> handler.findErrorRecordByUploadHistoryId("1"));
    }

    @Test
    void testUpdateErrorRecord_Success() throws Exception {
        UploadHistory history = getUploadHistoryWithErrors();
        when(uploadHistoryService.findById("1")).thenReturn(history);

        LeadDto dto = new LeadDto();
        Lead lead = new Lead();
        when(leadService.save(dto)).thenReturn(lead);
        when(uploadHistoryService.save(any())).thenReturn(history);

        LeadDto updated = handler.updateErrorRecord(1, "1", dto);

        assertNotNull(updated);
        assertEquals(UploadStatus.SUCCESS, history.getUploadStatus());
        verify(uploadHistoryService, atLeast(2)).save(any());
    }

    @Test
    void testUpdateErrorRecord_InvalidRow() throws Exception {
        UploadHistory history = getUploadHistoryWithErrors();
        when(uploadHistoryService.findById("1")).thenReturn(history);

        assertThrows(ErrorRecordException.class,
                () -> handler.updateErrorRecord(5, "1", new LeadDto()));
    }

    @Test
    void testUpdateUserErrorRecord_DuplicateEmail() throws Exception {
        UploadHistory history = getUploadHistoryWithErrors();
        when(uploadHistoryService.findById("1")).thenReturn(history);

        UserDTO dto = new UserDTO();
        dto.setEmail("dup@mail.com");
        dto.setMobileNumber("8888888888");

        when(userRepo.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(ErrorRecordException.class,
                () -> handler.updateUserErrorRecord(1, "1", dto));
    }

    @Test
    void testDeleteErrorRecordByEmail_Success() throws Exception {
        UploadHistory history = getUploadHistoryWithErrors();
        when(uploadHistoryService.findById("UP1")).thenReturn(history);
        when(uploadHistoryService.save(any())).thenReturn(history);

        handler.deleteErrorRecordByEmail(1, "UP1");

        assertEquals(UploadStatus.SUCCESS, history.getUploadStatus());
    }

    @Test
    void testDeleteErrorRecordByEmail_NoUploadHistoryError() {
        UploadHistory history = new UploadHistory();
        when(uploadHistoryService.findById("UP1")).thenReturn(history);

        assertThrows(UploadHistoryException.class,
                () -> handler.deleteErrorRecordByEmail(1, "UP1"));
    }
}
