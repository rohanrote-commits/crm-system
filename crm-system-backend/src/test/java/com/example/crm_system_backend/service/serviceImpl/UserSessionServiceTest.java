package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.entity.UserSession;
import com.example.crm_system_backend.repository.UserSessionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserSessionRepo userSessionRepo;

    @InjectMocks
    private UserSessionService userSessionService;


    @Test
    void testFindSessionByEmail_WhenSessionExists() {
        when(userSessionRepo.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = userSessionService.findSessionByEmail("test@example.com");

        assertTrue(result);
        verify(userSessionRepo).existsByEmail("test@example.com");
    }

    @Test
    void testFindSessionByEmail_WhenSessionDoesNotExist() {
        when(userSessionRepo.existsByEmail("test@example.com")).thenReturn(false);

        boolean result = userSessionService.findSessionByEmail("test@example.com");

        assertFalse(result);
        verify(userSessionRepo).existsByEmail("test@example.com");
    }


    @Test
    void testDeleteSessionByEmail_Success() {
        doNothing().when(userSessionRepo).deleteByEmail("test@example.com");

        assertDoesNotThrow(() -> userSessionService.deleteSessionByEmail("test@example.com"));

        verify(userSessionRepo, times(1)).deleteByEmail("test@example.com");
    }

    @Test
    void testDeleteSessionByEmail_WhenRepoThrowsException() {
        doThrow(new RuntimeException("DB error"))
                .when(userSessionRepo).deleteByEmail("test@example.com");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userSessionService.deleteSessionByEmail("test@example.com")
        );

        assertEquals("DB error", ex.getMessage());
        verify(userSessionRepo).deleteByEmail("test@example.com");
    }


    @Test
    void testSaveSession_Success() {
        UserSession session = new UserSession();
        when(userSessionRepo.save(session)).thenReturn(session);

        userSessionService.saveSession(session);

        verify(userSessionRepo, times(1)).save(session);
    }

    @Test
    void testSaveSession_WhenRepoThrowsException() {
        UserSession session = new UserSession();

        when(userSessionRepo.save(session))
                .thenThrow(new RuntimeException("Save failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userSessionService.saveSession(session)
        );

        assertEquals("Save failed", ex.getMessage());
        verify(userSessionRepo).save(session);
    }


    @Test
    void testFindSessionByToken_WhenSessionExists() {
        when(userSessionRepo.existsByTokenAndEmail("abc123", "test@example.com"))
                .thenReturn(true);

        boolean result = userSessionService.findSessionByToken("abc123", "test@example.com");

        assertTrue(result);
        verify(userSessionRepo).existsByTokenAndEmail("abc123", "test@example.com");
    }

    @Test
    void testFindSessionByToken_WhenSessionDoesNotExist() {
        when(userSessionRepo.existsByTokenAndEmail("abc123", "test@example.com"))
                .thenReturn(false);

        boolean result = userSessionService.findSessionByToken("abc123", "test@example.com");

        assertFalse(result);
        verify(userSessionRepo).existsByTokenAndEmail("abc123", "test@example.com");
    }

    @Test
    void testFindSessionByToken_WhenRepoThrowsException() {
        when(userSessionRepo.existsByTokenAndEmail("abc123", "test@example.com"))
                .thenThrow(new RuntimeException("Token check failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userSessionService.findSessionByToken("abc123", "test@example.com")
        );

        assertEquals("Token check failed", ex.getMessage());
        verify(userSessionRepo).existsByTokenAndEmail("abc123", "test@example.com");
    }
}
