package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.exception.ReportException;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import java.util.*;
import static com.example.crm_system_backend.constants.Roles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(ReportExcelHelper.class)
public class ReportExcelHelperTest {

    @Mock
    private ILeadRepository leadRepo;

    @Mock
    private IUserRepo userRepo;

    @Mock
    private DownloadReportHistoryRepo historyRepo;

    @InjectMocks
    private ReportExcelHelper reportExcelHelper;

    @BeforeEach     // JUnit 5 Annotation
    void setUp(){   // Runs before every @Test executes
        reportExcelHelper = spy(reportExcelHelper);
    }

    Date start = createDate(2025, 10, 1);
    Date end = createDate(2025, 10, 6);

    // Helper method to create date
    private Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    // ----- getLeadList -----
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
        assertTrue(leadListReceived.containsAll(leadListExpected) && leadListExpected.containsAll(leadListReceived), "The returned list should contain all expected leads.");
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
       assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as all dates are outside the range.");
    }

    @Test
    void getLeadList_fail_nullStartDate() {

        List<Lead> leadListReceived = reportExcelHelper.getLeadList(null , end);
        assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as start date is null");

    }

    @Test
    void getLeadList_fail_nullEndDate() {

        List<Lead> leadListReceived = reportExcelHelper.getLeadList(start , null);
        assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as end date is null");

    }

    @Test
    void getLeadList_fail_nullDates() {

        List<Lead> leadListReceived = reportExcelHelper.getLeadList(null, null);
        assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as start date and end date is null");

    }


    // ----- getName -----
    @Test
    void getName_success() {
        String inputEmail = "abc@gmail.com";
        String expectedFirstName = "abc";
        String expectedLastName = "pqr";
        String expectedName = expectedFirstName + " " + expectedLastName;

        List<User> users = Arrays.asList(
                new User("stu@gmail.com", "stu", "vwx"),
                new User(inputEmail, expectedFirstName, expectedLastName),
                new User("xyz@gmail.com", "xyz", "pqr")
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

        List<User> users = Arrays.asList(                //users- Replacement for userRepo.findAll()
                new User("stu@gmail.com", "stu", "vwx"),
                new User("pqr@gmail.com", "pqr", "stu"),
                new User("xyz@gmail.com",  "xyz", "pqr")
        );

        when(userRepo.findAll()).thenReturn(users);
        String actualName = reportExcelHelper.getName(inputEmail);

        assertNull(actualName, "The returned name must be null if the user is not found.");

        // Assert: Verify no further repository calls were made after the loop finished
        // This confirms the expensive findUser methods were never executed. Ensures efficiency
         verify(userRepo, never()).findUserFirstNameByEmail(anyString());
         verify(userRepo, never()).findUserLastNameByEmail(anyString());
    }

    @Test
    void getName_fail_emptyEmail() {
        String inputEmail = "";
        assertNull(reportExcelHelper.getName(inputEmail),  "The returned name must be empty as email is null.");
    }

    @Test
    void getName_fail_namesAreNull() {
        String inputEmail = "abc@gmail.com";

        List<User> users = Arrays.asList(
                new User("pqr@gmail.com", "pqr", "stu"),
                new User(inputEmail, null, null),
                new User("xyz@gmail.com", "xyz", "pqr")
        );

        // 1. Arrange: Stub the dependencies
        when(userRepo.findAll()).thenReturn(users);

        // 2. Act
        String actualName = reportExcelHelper.getName(inputEmail);

        // 3. Assert: Check for the exact concatenated string
        assertEquals("null null", actualName, "The returned name must be null if the user's name fields are null.");

        verify(userRepo, times(1)).findAll();
        verify(userRepo, times(1)).findUserFirstNameByEmail(inputEmail);
        verify(userRepo, times(1)).findUserLastNameByEmail(inputEmail);
    }


    // ----- getLeads -----
    @Test
    void getLeads_Success() {

        // Dummy data for testing
        User user1 = new User(1111101L, MASTER_ADMIN, 0L);
        User user2 = new User(1111102L, ADMIN, 1111101L);
        User user3 = new User(1111103L, USER, 1111101L);
        User user4 = new User(1111104L, USER, 1111102L);

        Lead lead1 = new Lead(1010101L, createDate(2025, 10, 10), user1);
        Lead lead2 = new Lead(2020202L, createDate(2025, 10, 5), user2);
        Lead lead3 = new Lead(3030303L, createDate(2025, 10, 1), user3);
        Lead lead4 = new Lead(4040404L, createDate(2025, 10, 20), user4);

        // Defining Mock Inputs
        List<Lead> leadList = List.of(lead2, lead3);     // returned by getLeadList
        List<Lead> allLeadsFromRepo = Arrays.asList(lead1, lead2, lead3, lead4);
        List<User> allUsersFromRepo = Arrays.asList(user1, user2, user3, user4);

        // Expected output
        Set<Lead> expectedFilteredLeads = new HashSet<>(Arrays.asList(lead2, lead3, lead4));

        doReturn(leadList).when(reportExcelHelper).getLeadList(start, end);
        when(leadRepo.findAll()).thenReturn(allLeadsFromRepo);
        when(userRepo.findAll()).thenReturn(allUsersFromRepo);

        Set<Lead> actualLeads = reportExcelHelper.getLeads(start, end);

        assertEquals(expectedFilteredLeads.size(), actualLeads.size(), "The expected size of leads set should match the actual size of leads set.");
        assertEquals(expectedFilteredLeads, actualLeads, "The expected leads set should match the actual leads set.");

        verify(reportExcelHelper, times(1)).getLeads(start, end);
        verify(userRepo, times(1)).findAll();
        verify(leadRepo, times(1)).findAll();
    }

    @Test
    void getLeads_fail_emptyUserList() {
        doReturn(null).when(reportExcelHelper).getLeads(start, end);
        assertNull(reportExcelHelper.getLeads(start, end), "Leads list should be empty if no users have registered");
    }

    @Test
    void getLeads_fail_emptyLeadList() {

        Set<Lead> emptyLeadSet = Collections.emptySet();
        doReturn(emptyLeadSet).when(reportExcelHelper).getLeads(start, end);

        Set<Lead> expectedLeads = Collections.emptySet();
        Set<Lead> actualLeads = reportExcelHelper.getLeads(start, end);

        assertEquals(expectedLeads, actualLeads, "Leads list should be empty if no leads are registered");

        verify(reportExcelHelper, times(1)).getLeads(start, end);
        verify(userRepo, never()).findAll();
        verify(leadRepo, never()).findAll();

    }  // no necessary that user list will also be empty



    // ----- getFilteredDownloadHistory -----
    @Test
    void getFilteredDownloadHistory_Success() {

            // ARRANGE

            // Define Test Data
            Long userId = 1010101L;
            String UserEmail = "ma@gmail.com";

            User user1 = new User(userId, MASTER_ADMIN, 0L, UserEmail);
            User user2 = new User(2020202L, ADMIN, userId, "adm@gmail.com");
            User user3 = new User(3030303L, USER, 2020202L, "admu@gmail.com");
            User user4 = new User(4040404L, USER, 1010102L, "mau@gmail.com");

            List<User> allUsers = Arrays.asList(user1, user2, user3, user4);

            downloadReport dr1 = new downloadReport(101L, "ma@gmail.com");
            downloadReport dr2 = new downloadReport(202L, "adm@gmail.com");
            downloadReport dr3 = new downloadReport(303L, "admu@gmail.com");
            downloadReport dr4 = new downloadReport(404L, "mau@gmail.com");

            List<downloadReport> allReports = Arrays.asList(dr1, dr2, dr3, dr4);

            Set<downloadReport> expectedRecords = Set.of(dr1, dr2, dr3);

            // Mock Repository Calls
            when(userRepo.findAll()).thenReturn(allUsers);
            when(historyRepo.findAll()).thenReturn(allReports);

            // ACT
            Set<downloadReport> actualRecords = reportExcelHelper.getFilteredDownloadHistory(
                    userId, "MASTER_ADMIN", UserEmail
            );

            // ASSERT
            assertNotNull(actualRecords, "Actual records should not be null");
            assertEquals(expectedRecords.size(), actualRecords.size(), "The number of filtered records should match the expected size.");
            assertTrue(actualRecords.containsAll(expectedRecords), "The filtered records should contain all expected reports.");
    }

    @Test
    void getFilteredDownloadHistory_fail_emptyUserList() {

        Long UserId = 1010101L;
        String role = MASTER_ADMIN.getDescription();
        String email = "abc@gmail.com";

        assertThrows(ReportException.class, () -> reportExcelHelper.getFilteredDownloadHistory(UserId, role, email), "Should throw Lead not found exception");

    }

}