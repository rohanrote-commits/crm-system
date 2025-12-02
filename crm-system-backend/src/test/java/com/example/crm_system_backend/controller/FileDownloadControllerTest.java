package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.FileDownloadException;
import com.example.crm_system_backend.handler.DownloadHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
@WebMvcTest(FileDownloadController.class)
public class FileDownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private DownloadHandler downloadHandler;

    @Test
    @DisplayName("Should successfully download user template")
    void downloadUserTemplate_success() throws Exception {
        byte[] fileContent = "Dummy content".getBytes();

        when(downloadHandler.downloadUserTemplate()).thenReturn(fileContent);

        mockMvc.perform(get("/crm/files/user-template"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-template.xlsx"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(content().bytes(fileContent));
    }

    @Test
    @DisplayName("Should successfully download lead template")
    void downloadLeadTemplate_success() throws Exception {
        byte[] fileContent = "Lead content".getBytes();

        when(downloadHandler.downloadLeadTemplate()).thenReturn(fileContent);

        mockMvc.perform(get("/crm/files/lead-template"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lead-template.xlsx"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(fileContent));
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error if FileDownloadException is thrown")
    void downloadUserTemplate_failure() throws Exception {
        when(downloadHandler.downloadUserTemplate()).thenThrow(new FileDownloadException(ErrorCode.ERROR_IN_FILE_DOWNLOAD));
        mockMvc.perform(get("/crm/files/user-template"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error if FileDownloadException is thrown for lead template")
    void downloadLeadTemplate_failure() throws Exception {
        when(downloadHandler.downloadLeadTemplate()).thenThrow(new FileDownloadException(ErrorCode.ERROR_IN_FILE_DOWNLOAD));

        mockMvc.perform(get("/crm/files/lead-template"))
                .andExpect(status().isInternalServerError());
    }
}
