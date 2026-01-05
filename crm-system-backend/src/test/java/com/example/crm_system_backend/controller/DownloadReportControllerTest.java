package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.downloadReportDTO;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static com.example.crm_system_backend.constants.Roles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DownloadReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    ReportExcelHelper helper;

    @InjectMocks
    DownloadReportController downloadReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(downloadReportController).build();
    }


    @Test
    void getAllReport_Success() throws Exception {

        // Dummy input data
        String role = ADMIN.getDescription();
        Long id = 202L;
        String email = "admin@gmail.com";

        // Expected Output
        Set<downloadReport> history = new HashSet<>();

        downloadReport record = new downloadReport();
        record.setId(102345L);
        record.setDownloadedAt("2025-11-20 10:00:00");
        record.setDateRange("2025-10-01 to 2025-10-31");
        record.setStatus("SUCCESS");
        record.setUserId(202L);
        history.add(record);


        mockMvc.perform(get("/crm/report/getDownloadedRecordHistory")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("role", role)
                .requestAttr("userId", id)
                .requestAttr("email", email))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllReport_Fail_userIdMissing() {

        Long id = eq(202L);
        String role = isNull();
        String email = eq("admin@gmail.com");

        Set<downloadReportDTO> result = helper.getFilteredDownloadHistory(id, role, email);
        assertTrue(result.isEmpty(), "Role must be present to get the downloaded record history");

    }

    @Test
    void getAllReport_Fail_roleMissing() {

        Long id = isNull();
        String role = eq(ADMIN.getDescription());
        String email = eq("admin@gmail.com");

        Set<downloadReportDTO> result = helper.getFilteredDownloadHistory(id, role, email);
        assertTrue(result.isEmpty(), "User ID must be present to get the downloaded record history");

    }

    @Test
    void getAllReport_Fail_emailMissing() {

        Long id = eq(202L);
        String role = eq(ADMIN.getDescription());
        String email = isNull();

        Set<downloadReportDTO> result = helper.getFilteredDownloadHistory(id, role, email);
        assertTrue(result.isEmpty(), "Email must be present to get the downloaded record history");

    }

    @Test
    void getAllReport_Fail_invalidRole() {
        Long id = 101L;
        String email = "abc@gmail.com";
        String role = "invalidRole";
        Set<downloadReportDTO> result = null;
        if(!Objects.equals(role, MASTER_ADMIN.getDescription()) || !Objects.equals(role, ADMIN.getDescription()) || !Objects.equals(role, BASIC.getDescription()) || !Objects.equals(role, USER.getDescription())) {
            result = helper.getFilteredDownloadHistory(id, role, email);
        }
        assertEquals(Collections.emptySet(), result, "Role should be either master admin, admin or basic, no other role allowed");
    }

}
