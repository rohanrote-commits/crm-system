package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.FileTemplateType;
import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UploadedHistoryHandlerTest {

    @Mock
    private UploadHistoryService uploadHistoryService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UploadedHistoryHandler uploadedHistoryHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper method to create UploadHistory entity
    private UploadHistory createHistory(String id, FileTemplateType type) {
        UploadHistory history = new UploadHistory();
        history.setId(id);
        history.setFileTemplateType(type);
        return history;
    }

    // Helper method to map UploadHistory -> DTO
    private UploadHistoryDto createDto(String id) {
        UploadHistoryDto dto = new UploadHistoryDto();
        dto.setId(id);
        return dto;
    }

    /**
     * Test findLeadUploadHistoryByEmail - SUCCESS
     */
    @Test
    void testFindLeadUploadHistoryByEmail_Success() {

        String email = "test@example.com";

        UploadHistory h1 = createHistory("1", FileTemplateType.LEAD);
        UploadHistory h2 = createHistory("2", FileTemplateType.USER);

        when(uploadHistoryService.findByUser(email)).thenReturn(List.of(h1, h2));
        when(modelMapper.map(eq(h1), eq(UploadHistoryDto.class)))
                .thenReturn(createDto("1"));

        List<UploadHistoryDto> result = uploadedHistoryHandler.findLeadUploadHistoryByEmail(email);

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        verify(uploadHistoryService, times(1)).findByUser(email);
    }


    /**
     * Test findUserUploadHistoryByEmail - SUCCESS
     */
    @Test
    void testFindUserUploadHistoryByEmail_Success() {

        String email = "user@test.com";

        UploadHistory h1 = createHistory("10", FileTemplateType.USER);
        UploadHistory h2 = createHistory("11", FileTemplateType.LEAD);

        when(uploadHistoryService.findByUser(email)).thenReturn(List.of(h1, h2));
        when(modelMapper.map(eq(h1), eq(UploadHistoryDto.class)))
                .thenReturn(createDto("10"));

        List<UploadHistoryDto> result = uploadedHistoryHandler.findUserUploadHistoryByEmail(email);

        assertEquals(1, result.size());
        assertEquals("10", result.get(0).getId());
    }


    /**
     * Test findUserUploadHistoryByEmail - SERVICE throws exception
     */
    @Test
    void testFindUserUploadHistoryByEmail_UserException() {

        String email = "invalid@test.com";

        when(uploadHistoryService.findByUser(email))
                .thenThrow(new UserException(ErrorCode.USER_NOT_FOUND));

        assertThrows(UserException.class, () ->
                uploadedHistoryHandler.findUserUploadHistoryByEmail(email)
        );
        verify(uploadHistoryService, times(1)).findByUser(email);
    }


    /**
     * Test findUploadHistoryById - SUCCESS
     */
    @Test
    void testFindUploadHistoryById_Success() {

        String id = "55";
        UploadHistory history = createHistory(id, FileTemplateType.LEAD);
        UploadHistoryDto dto = createDto(id);

        when(uploadHistoryService.findById(id)).thenReturn(history);
        when(modelMapper.map(history, UploadHistoryDto.class)).thenReturn(dto);

        UploadHistoryDto result = uploadedHistoryHandler.findUploadHistoryById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(uploadHistoryService, times(1)).findById(id);
    }
}
