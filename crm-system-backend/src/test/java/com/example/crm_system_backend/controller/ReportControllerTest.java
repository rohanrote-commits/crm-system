package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.ReportService;
import com.example.crm_system_backend.utils.JwtUtil;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @Mock
    private ReportExcelHelper helper;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(
                reportService,
                helper,
                jwtUtil
        ))
        .build();
    }

    @Test
    void getTemplate_Success() throws Exception {

        // --- ARRANGE ---

        // 1. Define inputs and expected outputs
        String email = "testuser@example.com";
        String token = "valid-jwt-token";
        String authorizationHeader = "Bearer " + token;

        // Dates for URL parameters (must be in "yyyy-MM-dd" format)
        String startDateString = "2025-01-01";
        String endDateString = "2025-10-31";

        // Convert date strings to java.util.Date objects for Mockito verification
        LocalDate localStart = LocalDate.of(2025, 1, 1);
        LocalDate localEnd = LocalDate.of(2025, 10, 31);
        Date expectedStartDate = Date.from(localStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expectedEndDate = Date.from(localEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Create a non-empty list of leads
        Set<Lead> mockLeadList = new HashSet<>();
        mockLeadList.add(new Lead(101L, "lead1@gmail.com"));

        // 2. Mock service behavior
        when(helper.getLeads(any(Date.class), any(Date.class))).thenReturn(mockLeadList);
        when(jwtUtil.getEmail(token)).thenReturn(email);

        @SuppressWarnings("unchecked")
        ResponseEntity<StreamingResponseBody> mockResponse = (ResponseEntity<StreamingResponseBody>) mock(ResponseEntity.class);
        when(reportService.excelToZipConverter(eq(mockLeadList), eq(expectedStartDate), eq(expectedEndDate)))
                .thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));


        // --- ACT & ASSERT ---
        mockMvc.perform(post("/crm/report/getTemplate")
                        .param("start", startDateString)
                        .param("end", endDateString)
                        .header("Authorization", authorizationHeader)
                )
                .andExpect(status().isOk());


        // --- VERIFICATION ---
        verify(helper, times(1)).getLeads(eq(expectedStartDate), eq(expectedEndDate));
        verify(jwtUtil, times(1)).getEmail(token);
        verify(reportService, times(1)).saveInDb(eq(expectedStartDate), eq(expectedEndDate), eq(email));
        verify(reportService, times(1)).excelToZipConverter(eq(mockLeadList), eq(expectedStartDate), eq(expectedEndDate));
    }

    @Test
    void getTemplate_Fail_NoLeadsRegistered() throws Exception {

        // --- ARRANGE ---
        String token = "valid-token";
        String authorizationHeader = "Bearer " + token;
        String startDateString = "2025-01-01";
        String endDateString = "2025-12-09";

        when(helper.getLeads(any(Date.class), any(Date.class))).thenReturn(new HashSet<>());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/crm/report/getTemplate") // Use your actual endpoint
                        .param("start", startDateString)
                        .param("end", endDateString)
                        .header("Authorization", authorizationHeader)
                )
                .andExpect(status().isNoContent());

        // --- VERIFICATION ---
        verify(reportService, never()).saveInDb(any(), any(), any());
        verify(reportService, never()).excelToZipConverter(any(), any(), any());
        verify(helper, times(1)).getLeads(any(), any());

    }

    @Test
    void getTemplate_Fail_InvalidTokenThrowsException() throws Exception {

        // --- ARRANGE ---
        String token = "bad-or-expired-token";
        String authorizationHeader = "Bearer " + token;

        Set<Lead> mockLeadList = new HashSet<>();
        mockLeadList.add(new Lead(101L, "lead1@gmail.com"));
        when(helper.getLeads(any(), any())).thenReturn(mockLeadList);
        doThrow(new SignatureException("JWT signature validation failed"))
                .when(jwtUtil).getEmail(eq(token));

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/crm/report/getTemplate")
                        .param("start", "2025-01-01")
                        .param("end", "2025-10-31")
                        .header("Authorization", authorizationHeader)
                )
                .andExpect(status().isUnauthorized());

        // --- VERIFICATION ---
        verify(reportService, never()).saveInDb(any(), any(), any());
        verify(reportService, never()).excelToZipConverter(any(), any(), any());
    }

    @Test
    void getTemplate_Fail_CouldNotSaveInDb() throws Exception {

        // --- ARRANGE ---
        String token = "valid-token";
        String authorizationHeader = "Bearer " + token;
        String email = "test@example.com";

        Set<Lead> mockLeadList = new HashSet<>();
        mockLeadList.add(new Lead(101L, "lead1@gmail.com"));

        when(helper.getLeads(any(), any())).thenReturn(mockLeadList);
        when(jwtUtil.getEmail(eq(token))).thenReturn(email);

        doThrow(new RuntimeException("Simulated database write failure"))
                .when(reportService).saveInDb(any(), any(), eq(email));

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/crm/report/getTemplate")
                        .param("start", "2025-01-01")
                        .param("end", "2025-10-31")
                        .header("Authorization", authorizationHeader)
                )
                .andExpect(status().isInternalServerError());

        // --- VERIFICATION ---
        verify(reportService, times(1)).saveInDb(any(), any(), eq(email));
        verify(reportService, never()).excelToZipConverter(any(), any(), any());
    }

}
