package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.ReportService;
import com.example.crm_system_backend.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
public class ReportControllerTest {


    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @Mock
    private ReportExcelHelper helper;

    @Mock
    private JwtUtil jwtUtil;


    @Test
    void getTemplate_Success() {

    }

    @Test
    void getTemplate_Fail_AuthorizationTokenMissing() throws Exception {
    }

    @Test
    void getTemplate_Fail_AuthorizationInvalid() {
    }

    @Test
    void getTemplate_Fail_NoLeadsRegistered() {

    }

    @Test
    void getTemplate_Fail_CouldNotFindEmail() {
    }

    @Test
    void getTemplate_Fail_CouldNotSaveInDb() {
    }

    @Test
    void getTemplate_Fail_returnsNull() {
    }
}
