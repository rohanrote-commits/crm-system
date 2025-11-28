package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportExcelHelperTest {

    @Mock
    private ILeadRepository leadRepo;

    @Mock
    private IUserRepo userRepo;

    @InjectMocks
    private ReportExcelHelper reportExcelHelper;

    Date start = createDate(2025, 10, 1);
    Date end = createDate(2025, 11, 1);

    // Helper method to create date
    private Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Test
    void getLeadList_success() {
        // 1. Arrange (giving dummy input)
        List<Lead> allLeads = Arrays.asList(     // replaces leadRepo.findAll()
                new Lead(1010101L, createDate(2025, 10, 15)),  // Mid Range

                new Lead(1010102L, createDate(2025, 10, 2)),   // Just after start date
                new Lead(1010103L, createDate(2025, 10, 31)),  // Just before end date

                new Lead(1010104L, createDate(2025, 10, 1)),  // On start date
                new Lead(1010105L, createDate(2025, 11, 1)),  // On end Date

                new Lead(1010106L, createDate(2025, 9, 30)),  // Just before start date
                new Lead(1010107L, createDate(2025, 11, 2))   // Just after end date
        );


        // 2. Act (execute using dummy input)
        when(leadRepo.findAll()).thenReturn(allLeads);
        List<Lead> leadListReceived = reportExcelHelper.getLeadList(start, end);

        // 3. Add expected result
        List<Lead> leadListExpected = Arrays.asList(
                new Lead(1010101L, createDate(2025, 10, 15)),   // Mid Range
                new Lead(1010102L, createDate(2025, 10, 2)),    // Just after start date
                new Lead(1010103L, createDate(2025, 10, 31)),   // Just before end date
                new Lead(1010104L, createDate(2025, 10, 1))     // On start date
        );

        // 4. Assert (check if actual output received matches expected output)
        assertEquals(leadListExpected.size(), leadListReceived.size(), "The returned list size should match the expected list size.");
        assertEquals(leadListExpected, leadListReceived, "The returned list should match the expected list.");
    }

    @Test
    void getLeadList_fail_emptyList() {
       List<Lead> leads = Arrays.asList(
               new Lead(1010105L, createDate(2025, 11, 1)),  // On end Date
               new Lead(1010106L, createDate(2025, 9, 30)),  // Just before start date
               new Lead(1010107L, createDate(2025, 11, 2))   // Just after end date
       );

       when(leadRepo.findAll()).thenReturn(leads);
       List<Lead> leadListReceived = reportExcelHelper.getLeadList(start, end);
       assertEquals(0, leadListReceived.size(), "Leads List should not be empty");
    }

    @Test
    void getLeadList_fail_nullStartDate() {
        Date date = createDate(0, 0, 0);
        List<Lead> leadListReceived = reportExcelHelper.getLeadList(date , end);

        assertEquals(0, leadListReceived.size(), "Start date should not be empty");
    }

    @Test
    void getLeadList_fail_nullEndDate() {
        Date date = createDate(0, 0, 0);
        List<Lead> leadListReceived = reportExcelHelper.getLeadList(start , date);

        assertEquals(0, leadListReceived.size(), "end date should not be empty");
    }

    @Test
    void getLeadList_fail_nullDates() {
        Date date = createDate(0, 0, 0);
        List<Lead> leadListReceived = reportExcelHelper.getLeadList(date , date);

        assertEquals(0, leadListReceived.size(), "Start date and end date must be defined");
    }


    @Test
    void getName_success() {
        String inputEmail = "abc@gmail.com";
        String expectedFirstName = "abc";
        String expectedLastName = "pqr";
        String expectedName = expectedFirstName + " " + expectedLastName;

        List<User> users = Arrays.asList(
                new User("stu@gmail.com"),
                new User(inputEmail),
                new User("xyz@gmail.com")
        );

        when(userRepo.findAll()).thenReturn(users);
        when(userRepo.findUserFirstNameByEmail(inputEmail)).thenReturn(expectedFirstName);
        when(userRepo.findUserLastNameByEmail(inputEmail)).thenReturn(expectedLastName);

        String actualName = reportExcelHelper.getName(inputEmail);

        assertEquals(expectedName, actualName, "The returned name should match the expected full name.");
    }

    @Test
    void getName_fail_emailNotFound() {
        String inputEmail = "abc@gmail.com";

        List<User> users = Arrays.asList(            //users- Replacement for userRepo.findAll()
                new User("stu@gmail.com"),
                new User("pqr@gmail.com"),
                new User("xyz@gmail.com")
        );

        when(userRepo.findAll()).thenReturn(users);
        String actualName = reportExcelHelper.getName(inputEmail);

        assertNull(actualName, "The method should return null when the email is NOT found in the list.");

        // Assert: Verify no further repository calls were made after the loop finished
        // This confirms the expensive findUser methods were never executed. Ensures efficiency
        verify(userRepo, never()).findUserFirstNameByEmail(anyString());
        verify(userRepo, never()).findUserLastNameByEmail(anyString());
    }

    @Test
    void getName_fail_emptyEmail() {
        String inputEmail = "";
        assertNull(reportExcelHelper.getName(inputEmail),  "The email should not be empty.");
    }

    @Test
    void getName_returnsNullString_whenNamesAreNull() {
        String inputEmail = "abc@gmail.com";
        String expectedName = "null null";   //The expected output, due to String concatenation of nulls

        List<User> users = Arrays.asList(
                new User("pqr@gmail.com"),
                new User(inputEmail),
                new User("xyz@gmail.com")
        );

        // 1. Arrange: Stub the dependencies
        when(userRepo.findAll()).thenReturn(users);
        when(userRepo.findUserFirstNameByEmail(inputEmail)).thenReturn(null);
        when(userRepo.findUserLastNameByEmail(inputEmail)).thenReturn(null);

        // 2. Act
        String actualName = reportExcelHelper.getName(inputEmail);

        // 3. Assert: Check for the exact concatenated string
        assertEquals(expectedName, actualName,
                "The method should return 'null null' due to Java's string concatenation when names are missing.");

        // Optional: Verify interaction with the repository
        verify(userRepo, times(1)).findAll();
        verify(userRepo, times(1)).findUserFirstNameByEmail(inputEmail);
        verify(userRepo, times(1)).findUserLastNameByEmail(inputEmail);
    }

}