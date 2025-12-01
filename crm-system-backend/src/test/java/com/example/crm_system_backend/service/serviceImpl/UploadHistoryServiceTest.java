package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.FileTemplateType;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.UploadHistoryException;
import com.example.crm_system_backend.repository.IUploadHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;


import java.util.List;
import java.util.Optional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadHistoryServiceTest {

    @InjectMocks
    private UploadHistoryService uploadHistoryService;

    @Mock
    private IUploadHistoryRepository iUploadHistoryRepository;

    @Mock
    private ModelMapper modelMapper;

    UploadHistory uploadHistory;

    @BeforeEach
    void setUp(){
        uploadHistory = new UploadHistory();

        uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
        uploadHistory.setFileTemplateType(FileTemplateType.LEAD);
        uploadHistory.setErrorRecord("[]");
        uploadHistory.setUploadedBy("test@example.com");
        uploadHistory.setFileName("test_file.xlsx");
        uploadHistory.setTotalRecords(100);
        uploadHistory.setValidRecords(90);
        uploadHistory.setInvalidRecords(10);
        uploadHistory.setErrorFileName("error_file.xlsx");
    }


    /**
     * Test case for verifying the successful save functionality of the `save` method.
     */
    @Test
    void testSave_Success() {
        // Arrange

        when(iUploadHistoryRepository.save(any(UploadHistory.class))).thenAnswer(invocation -> {
            UploadHistory savedEntity = invocation.getArgument(0);
            savedEntity.setId("1");
            return savedEntity;
        });

        // Act
        UploadHistory savedHistory = uploadHistoryService.save(uploadHistory);

        // Assert
        assertNotNull(savedHistory.getId(), "Saved entity must have an ID");
        assertEquals(uploadHistory.getUploadedBy(), savedHistory.getUploadedBy(), "UploadedBy value should match");
        assertEquals(uploadHistory.getFileName(), savedHistory.getFileName(), "FileName value should match");
        verify(iUploadHistoryRepository, times(1)).save(uploadHistory);
    }

    /**
     * Test case for verifying that calling save with a null object throws an exception.
     */
    @Test
    void testSave_NullInput_ThrowsException() {
        Mockito.when(iUploadHistoryRepository.save(null))
                .thenThrow(new IllegalArgumentException());

        assertThrows(UploadHistoryException.class, () -> {
            uploadHistoryService.save(null);
        });
    }

    @Test
    void testFindById_Success() {
        String uploadId = UUID.randomUUID().toString();

        when(iUploadHistoryRepository.findById(uploadId))
                .thenReturn(Optional.of(uploadHistory));

        // Act
        UploadHistory result = uploadHistoryService.findById(uploadId);

        // Assert
        assertNotNull(result);
        assertEquals(uploadHistory.getFileName(), result.getFileName());
        assertEquals(uploadHistory.getUploadedBy(), result.getUploadedBy());
        verify(iUploadHistoryRepository, times(1)).findById(uploadId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        String uploadId = UUID.randomUUID().toString();
        when(iUploadHistoryRepository.findById(uploadId)).thenReturn(Optional.empty());

        // Assert
        ExcelException exception = assertThrows(ExcelException.class, () -> {
            // Act
            uploadHistoryService.findById(uploadId);
        });

        assertEquals("File History Not Found", exception.getMessage(),
                     "Error code should indicate FILE_HISTORY_NOT_FOUND");
        verify(iUploadHistoryRepository, times(1)).findById(uploadId);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        String id = UUID.randomUUID().toString();
        UploadHistory inputHistory = new UploadHistory();
        inputHistory.setId(id);
        inputHistory.setUploadStatus(UploadStatus.FAILED);

        uploadHistory.setId(id);

        when(iUploadHistoryRepository.findById(id)).thenReturn(Optional.of(uploadHistory));
        when(iUploadHistoryRepository.save(uploadHistory)).thenReturn(uploadHistory);

        // Act
        UploadHistory result = uploadHistoryService.update(inputHistory);

        // Assert
        assertNotNull(result);
        verify(iUploadHistoryRepository).findById(id);
        verify(modelMapper).map(inputHistory, uploadHistory);
        verify(iUploadHistoryRepository).save(uploadHistory);
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        String id = "123";
        UploadHistory inputHistory = new UploadHistory();
        inputHistory.setId(id);

        when(iUploadHistoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        ExcelException ex = assertThrows(ExcelException.class, () -> uploadHistoryService.update(inputHistory));
        assertEquals(ErrorCode.FILE_HISTORY_NOT_FOUND.getMessage(), ex.getMessage());
        verify(iUploadHistoryRepository).findById(id);
        verify(iUploadHistoryRepository, never()).save(any());
    }

    // ---------- deleteById ----------

    @Test
    void testDeleteById() {
        // Arrange
        String id = "123";
        doNothing().when(iUploadHistoryRepository).deleteById(id);

        // Act
        uploadHistoryService.deleteById(id);

        // Assert
        verify(iUploadHistoryRepository, times(1)).deleteById(id);
    }

    // ---------- findByUser ----------

    @Test
    void testFindByUser_Success() {
        // Arrange
        String email = "user@example.com";
        List<UploadHistory> historyList = List.of(uploadHistory);
        when(iUploadHistoryRepository.findByUploadedBy(email)).thenReturn(Optional.of(historyList));

        // Act
        List<UploadHistory> result = uploadHistoryService.findByUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(uploadHistory.getFileName(), result.get(0).getFileName());
        verify(iUploadHistoryRepository).findByUploadedBy(email);
    }

    @Test
    void testFindByUser_NotFound() {
        // Arrange
        String email = "unknown@example.com";
        when(iUploadHistoryRepository.findByUploadedBy(email)).thenReturn(Optional.empty());

        // Act & Assert
        ExcelException ex = assertThrows(ExcelException.class, () -> uploadHistoryService.findByUser(email));
        assertEquals(ErrorCode.FILE_HISTORY_NOT_FOUND.getMessage(), ex.getMessage());
        verify(iUploadHistoryRepository).findByUploadedBy(email);
    }

}
