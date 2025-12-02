//package com.example.crm_system_backend.helper;
//
//import com.example.crm_system_backend.entity.Lead;
//import com.example.crm_system_backend.entity.User;
//import com.example.crm_system_backend.repository.ILeadRepository;
//import com.example.crm_system_backend.repository.IUserRepo;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//
//import java.util.*;
//import static org.junit.jupiter.api.Assertions.*;
//
////@ExtendWith(MockitoExtension.class)
//@DataJpaTest
//@Import(ReportExcelHelper.class)
//public class ReportExcelHelperTest {
//
////    @Mock
//    @Autowired
//    private ILeadRepository leadRepo;
//
////    @Mock
//    @Autowired
//    private IUserRepo userRepo;
//
////    @InjectMocks
//    @Autowired
//    private ReportExcelHelper reportExcelHelper;
//
//    Date start = createDate(2025, 10, 1);
//    Date end = createDate(2025, 11, 1);
//
//    // Helper method to create date
//    private Date createDate(int year, int month, int day) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month-1, day, 0, 0, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTime();
//    }
//
//
//    // ----- getLeadList -----
//
//    @Test
//    @Transactional
//    void getLeadList_success() {
//        // 1. Arrange (giving dummy input)
//        List<Lead> allLeads = Arrays.asList(     // replaces leadRepo.findAll()
//                new Lead(1010101L, createDate(2025, 10, 15)),  // Mid Range
//
//                new Lead(1010102L, createDate(2025, 10, 2)),   // Just after start date
//                new Lead(1010103L, createDate(2025, 10, 31)),  // Just before end date
//
//                new Lead(1010104L, createDate(2025, 10, 1)),  // On start date
//                new Lead(1010105L, createDate(2025, 11, 1)),  // On end Date
//
//                new Lead(1010106L, createDate(2025, 9, 30)),  // Just before start date
//                new Lead(1010107L, createDate(2025, 11, 2))   // Just after end date
//        );
//
//
//        // 2. Act (execute using dummy input)
////        when(leadRepo.findAll()).thenReturn(allLeads);
////        List<Lead> leadListReceived = reportExcelHelper.getLeadList(start, end);
//
//        leadRepo.saveAll(allLeads);
//
//        // 3. Add expected result
//        List<Lead> leadListExpected = Arrays.asList(
//                new Lead(1010101L, createDate(2025, 10, 15)),   // Mid Range
//                new Lead(1010102L, createDate(2025, 10, 2)),    // Just after start date
//                new Lead(1010103L, createDate(2025, 10, 31)),   // Just before end date
//                new Lead(1010104L, createDate(2025, 10, 1))     // On start date
//        );
//
//        List<Lead> leadListReceived = reportExcelHelper.getLeadList(start, end);
//
//        // 4. Assert (check if actual output received matches expected output)
//        assertEquals(leadListExpected.size(), leadListReceived.size(), "The returned list size should match the expected list size.");
//        assertTrue(leadListReceived.containsAll(leadListExpected) && leadListExpected.containsAll(leadListReceived), "The returned list should contain all expected leads.");
//    }
//
//    @Test
//    void getLeadList_fail_emptyList() {
//       List<Lead> leads = Arrays.asList(
//               new Lead(1010105L, createDate(2025, 11, 1)),  // On end Date
//               new Lead(1010106L, createDate(2025, 9, 30)),  // Just before start date
//               new Lead(1010107L, createDate(2025, 11, 2))   // Just after end date
//       );
//
//       leadRepo.saveAll(leads);
//       List<Lead> leadListReceived = reportExcelHelper.getLeadList(start, end);
//       assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as all dates are outside the range.");
//    }
//
//    @Test
//    void getLeadList_fail_nullStartDate() {
//
//        List<Lead> leadListReceived = reportExcelHelper.getLeadList(null , end);
//        assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as start date is null");
//
//    }
//
//    @Test
//    void getLeadList_fail_nullEndDate() {
//
//        List<Lead> leadListReceived = reportExcelHelper.getLeadList(start , null);
//        assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as end date is null");
//
//    }
//
//    @Test
//    void getLeadList_fail_nullDates() {
//
//        List<Lead> leadListReceived = reportExcelHelper.getLeadList(null, null);
//        assertEquals(0, leadListReceived.size(), "The returned Leads list must be empty as start date and end date is null");
//
//    }
//
//
//    // ----- getName -----
//
//    @Test
//    void getName_success() {
//        String inputEmail = "abc@gmail.com";
//        String expectedFirstName = "abc";
//        String expectedLastName = "pqr";
//        String expectedName = expectedFirstName + " " + expectedLastName;
//
//        List<User> users = Arrays.asList(
//                new User("stu@gmail.com", "stu", "vwx"),
//                new User(inputEmail, expectedFirstName, expectedLastName),
//                new User("xyz@gmail.com", "xyz", "pqr")
//        );
//
//        userRepo.saveAll(users);
//        String actualName = reportExcelHelper.getName(inputEmail);
//        assertEquals(expectedName, actualName, "The returned name should match the expected full name.");
//    }
//
//    @Test
//    void getName_fail_emailNotFound() {
//        String inputEmail = "abc@gmail.com";
//
//        List<User> users = Arrays.asList(            //users- Replacement for userRepo.findAll()
//                new User("stu@gmail.com", "stu", "vwx"),
//                new User("pqr@gmail.com", "pqr", "stu"),
//                new User("xyz@gmail.com",  "xyz", "pqr")
//        );
//
//        userRepo.saveAll(users);
//        String actualName = reportExcelHelper.getName(inputEmail);
//
//        assertNull(actualName, "The returned name must be null if the user is not found.");
//
//        // Assert: Verify no further repository calls were made after the loop finished
//        // This confirms the expensive findUser methods were never executed. Ensures efficiency
//        // verify(userRepo, never()).findUserFirstNameByEmail(anyString());
//        // verify(userRepo, never()).findUserLastNameByEmail(anyString());
//    }
//
//    @Test
//    void getName_fail_emptyEmail() {
//        String inputEmail = "";
//        assertNull(reportExcelHelper.getName(inputEmail),  "The returned name must be empty as email is null.");
//    }
//
//    @Test
//    void getName_fail_namesAreNull() {
//        String inputEmail = "abc@gmail.com";
//
//        List<User> users = Arrays.asList(
//                new User("pqr@gmail.com", "pqr", "stu"),
//                new User(inputEmail, null, null),
//                new User("xyz@gmail.com", "xyz", "pqr")
//        );
//
//        // 1. Arrange: Stub the dependencies
//        userRepo.saveAll(users);
//
//        // 2. Act
//        String actualName = reportExcelHelper.getName(inputEmail);
//
//        // 3. Assert: Check for the exact concatenated string
//        assertNull(actualName, "The returned name must be null if the user's name fields are null.");
//
////        verify(userRepo, times(1)).findAll();
////        verify(userRepo, times(1)).findUserFirstNameByEmail(inputEmail);
////        verify(userRepo, times(1)).findUserLastNameByEmail(inputEmail);
//    }
//
//
//    // ----- getLeads -----
//
//}