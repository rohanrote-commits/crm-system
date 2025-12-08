//package com.example.crm_system_backend.controller;
//
//import com.example.crm_system_backend.entity.downloadReport;
//import com.example.crm_system_backend.helper.ReportExcelHelper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Set;
//
//import static com.example.crm_system_backend.constants.Roles.ADMIN;
//
//@ExtendWith(MockitoExtension.class)
//public class DownloadReportControllerTest {
//
//    @Mock
//    ReportExcelHelper helper;
//
//    @Test
//    void getAllReport_Success() {
//        String role = ADMIN.getDescription();
//        Long id = 1010101L;
//        String email = "abc@gmail.com";
//
//        Set<downloadReport> filteredHistoryRecords = helper.getFilteredDownloadHistory(id, role, email);
//    }
//    @Test
//    void getAllReport_Fail_RoleMissing() {
//    }
//    @Test
//    void getAllReport_Fail_userIdMissing() {
//    }
//    @Test
//    void getAllReport_Fail_emailMissing() {
//    }
//
//}
