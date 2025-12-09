package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.example.crm_system_backend.constants.Roles.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    // -----  ListToExcelStream -----

    @Test
    void ListToExcelStream_Success() {

        LocalDate localStart = LocalDate.of(2025, 1, 1);
        LocalDate localEnd = LocalDate.of(2025, 10, 31);
        Date start = Date.from(localStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(localEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());

        User user1 = new User(111L, MASTER_ADMIN, 0L, "abc@gmail.com");
        User user2 = new User(222L, ADMIN, 111L, "pqr@gmail.com");
        User user3 = new User(333L, BASIC, 111L, "xyz@gmail.com");
        User user4 = new User(444L, BASIC, 222L, "stu@gmail.com");

        Lead lead1 = new Lead(101L, "a@gmail.com");
        Lead lead2 = new Lead(102L, "b@gmail.com");
        Lead lead3 = new Lead(103L, "c@gmail.com");
        Lead lead4 = new Lead(104L, "d@gmail.com");

        Set<User> mockUserList = Set.of(user1, user2, user3, user4);
        Set<Lead> mockLeadList = Set.of(lead1, lead2, lead3, lead4);

    }

    @Test
    void ListToExcelStream_Fail_ErrorInSummaryReport() {}

    @Test
    void ListToExcelStream_Fail_ErrorInPerUserReport() {}

    // ----- excelToZipConverter -----

    @Test
    void excelToZipConverter_Success() {}

    @Test
    void excelToZipConverter_Fail_EmptyLeadSet() {}

    @Test
    void excelToZipConverter_Fail_ErrorInResponseBody() {}

    // ----- saveInDb -----

    @Test
    void saveInDb_success() {}

    @Test
    void saveInDb_Fail_ErrorSavingInDB() {}
    // in historyRepo.save(data);
    // may be some data is missing

    // ----- SummaryReport -----

    @Test
    void SummaryReport_success() {}

    @Test
    void SummaryReport_Fail() {}

    // ----- Per User Report -----

    @Test
    void PerUserReport_success() {}

    @Test
    void PerUserReport_Fail() {}

}
