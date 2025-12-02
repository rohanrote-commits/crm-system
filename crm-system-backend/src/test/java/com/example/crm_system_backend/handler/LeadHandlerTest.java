package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.LeadList;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.Product;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.helper.LeadExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.ProductService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LeadHandlerTest {

    @Mock
    private com.example.crm_system_backend.service.serviceImpl.LeadService leadService;

    @Mock
    private UserService userService;

    @Mock
    private LeadExcelHelper leadExcelHelper;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UploadHistoryService uploadHistoryService;

    @Mock
    private ErrorRecordHandler errorRecordHandler;

    @Mock
    private ProductService productService;

    @Mock
    private com.example.crm_system_backend.service.ILeadService iLeadService;

    @InjectMocks
    private LeadHandler leadHandler;

    // ---------- save() ----------
    @Test
    void save_ShouldSaveAndReturnDto_WhenLeadNotExists() {
        // Arrange
        LeadDto dto = new LeadDto();
        dto.setEmail("a@x.com");
        dto.setUser("user@x.com");

        Lead saved = new Lead();
        saved.setEmail(dto.getEmail());

        when(leadService.getLeadByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(leadService.save(dto)).thenReturn(saved);
        when(modelMapper.map(saved, LeadDto.class)).thenReturn(dto);

        // Act
        LeadDto result = leadHandler.save(dto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(dto.getEmail(), result.getEmail());
        verify(leadService).getLeadByEmail(dto.getEmail());
        verify(leadService).save(dto);
        verify(modelMapper).map(saved, LeadDto.class);
    }

    @Test
    void save_ShouldThrow_WhenLeadAlreadyExists() {
        LeadDto dto = new LeadDto();
        dto.setEmail("exists@x.com");

        Lead existing = new Lead();
        when(leadService.getLeadByEmail(dto.getEmail())).thenReturn(Optional.of(existing));

        LeadException ex = Assertions.assertThrows(LeadException.class, () -> leadHandler.save(dto));
        // Can't assert message, but ensure exception type
        verify(leadService).getLeadByEmail(dto.getEmail());
        verifyNoMoreInteractions(leadService);
    }

    // ---------- getLeadsByUser ----------
    @Test
    void getLeadsByUser_ShouldReturnDtos_WhenUsersFound() {
        // Arrange
        Long userId = 1L;
        User main = new User();
        main.setId(userId);
        main.setEmail("main@x.com");

        User sub = new User();
        sub.setId(2L);
        sub.setEmail("sub@x.com");

        Lead l1 = new Lead();
        Product p1 = new Product();
        p1.setProductName("CRM");
        l1.setInterestedProducts(Set.of(p1));

        when(userService.getUserById(userId)).thenReturn(Optional.of(main));
        when(userService.getAllUsersRegisterById(userId)).thenReturn(Optional.of(List.of(sub)));
        when(leadService.findByUserIn(argThat(list -> list.size() == 2))).thenReturn(List.of(l1));

        // modelMapper.map is called but its return is not used for mapping fields in getLeadsByUser (destination is new object)
        // no stubbing required

        // Act
        List<LeadDto> result = leadHandler.getLeadsByUser(userId);

        // Assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).getInterestedModules().contains("CRM"));
        verify(userService).getUserById(userId);
        verify(leadService).findByUserIn(anyList());
    }

    @Test
    void getLeadsByUser_ShouldThrow_WhenUserNotFound() {
        Long userId = 5L;
        when(userService.getUserById(userId)).thenReturn(Optional.empty());
        Assertions.assertThrows(UserException.class, () -> leadHandler.getLeadsByUser(userId));
        verify(userService).getUserById(userId);
    }

    // ---------- getLeadByEmail ----------
    @Test
    void getLeadByEmail_ShouldReturnLead_WhenFound() {
        String email = "lead@x.com";
        Lead lead = new Lead();
        lead.setEmail(email);
        when(leadService.getLeadByEmail(email)).thenReturn(Optional.of(lead));

        Lead result = leadHandler.getLeadByEmail(email);

        Assertions.assertEquals(email, result.getEmail());
        verify(leadService).getLeadByEmail(email);
    }

    @Test
    void getLeadByEmail_ShouldThrow_WhenNotFound() {
        String email = "missing@x.com";
        when(leadService.getLeadByEmail(email)).thenReturn(Optional.empty());
        Assertions.assertThrows(LeadException.class, () -> leadHandler.getLeadByEmail(email));
        verify(leadService).getLeadByEmail(email);
    }

    // ---------- getAll ----------
    @Test
    void getAll_ShouldReturnAllLeadDtos() {
        Lead l1 = new Lead();
        Product p = new Product();
        p.setProductName("M");
        l1.setInterestedProducts(Set.of(p));

        when(leadService.getAllLeads()).thenReturn(List.of(l1));

        List<LeadDto> result = leadHandler.getAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).getInterestedModules().contains("M"));
        verify(leadService).getAllLeads();
    }

    // ---------- edit ----------
    @Test
    void edit_ShouldUpdateAndReturnDto_WhenLeadExists() {
        Long leadId = 10L;
        LeadDto incoming = new LeadDto();
        incoming.setInterestedModules(Set.of("CRM"));

        Lead old = new Lead();
        old.setId(leadId);

        Product p = new Product();
        p.setProductName("CRM");

        LeadDto returnedDto = new LeadDto();
        returnedDto.setEmail("mapped@x.com");

        when(leadService.getLeadById(leadId)).thenReturn(Optional.of(old));
        // modelMapper.map(source, destination) is used to copy incoming -> old. It returns destination; we can no-op it.
        // productService.getProductByName called for each module
        when(productService.getProductByName("CRM")).thenReturn(p);
        // modelMapper.map(old, LeadDto.class) is used to return DTO
        lenient().when(modelMapper.map(old, LeadDto.class)).thenReturn(returnedDto);

        LeadDto result = leadHandler.edit(leadId, incoming);

        Assertions.assertEquals(returnedDto, result);
        verify(leadService).getLeadById(leadId);
        verify(productService).getProductByName("CRM");
        verify(leadService).editLead(eq(leadId), any(Lead.class));
    }

    @Test
    void edit_ShouldThrow_WhenLeadNotFound() {
        Long id = 99L;
        when(leadService.getLeadById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(LeadException.class, () -> leadHandler.edit(id, new LeadDto()));
        verify(leadService).getLeadById(id);
    }

    // ---------- delete ----------
    @Test
    void delete_ShouldCallServiceDelete() {
        Long id = 3L;
        doNothing().when(leadService).deleteLead(id);
        leadHandler.delete(id);
        verify(leadService).deleteLead(id);
    }

//    // ---------- bulkUpload ----------
//    @Test
//    void bulkUpload_ShouldProcessAndSaveUploadHistory_SuccessPath() throws Exception {
//        // Arrange
//        String fileName = "leads.xlsx";
//        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "dummy".getBytes());
//        Long userId = 7L;
//
//        User user = new User();
//        user.setId(userId);
//        user.setEmail("u@x.com");
//
//        Lead validLead = new Lead();
//
//        // Mock LeadList and behavior
//        LeadList leadListMock = mock(LeadList.class);
//        when(leadListMock.getValidLeadList()).thenReturn(List.of(validLead));
//        when(leadListMock.getInvalidLeadList()).thenReturn(Collections.emptyList());
//
//        when(userService.getUserById(userId)).thenReturn(Optional.of(user));
//        // processExcelData receives a newly created UploadHistory inside method; we don't need to match it exactly
//        when(leadExcelHelper.processExcelData(any(MultipartFile.class), any(UploadHistory.class))).thenReturn(Optional.of(leadListMock));
//
//        // Act
//        leadHandler.bulkUpload(file, userId);
//
//        // Assert
//        // capture the upload history saved
//        ArgumentCaptor<UploadHistory> captor = ArgumentCaptor.forClass(UploadHistory.class);
//        verify(uploadHistoryService).save(captor.capture());
//        UploadHistory saved = captor.getValue();
//        Assertions.assertEquals(UploadStatus.SUCCESS, saved.getUploadStatus());
//        verify(leadService).bulkUpload(anyList());
//    }

    @Test
    void bulkUpload_ShouldMarkFailedAndThrow_WhenProcessingThrows() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "bad.xlsx", "application/vnd.ms-excel", "x".getBytes());
        Long userId = 2L;

        User user = new User();
        user.setId(userId);
        user.setEmail("u2@x.com");

        when(userService.getUserById(userId)).thenReturn(Optional.of(user));
        when(leadExcelHelper.processExcelData(any(MultipartFile.class), any(UploadHistory.class))).thenThrow(new RuntimeException("boom"));

        LeadException ex = Assertions.assertThrows(LeadException.class, () -> leadHandler.bulkUpload(file, userId));
        // ensure uploadHistory saved with FAILED status
        ArgumentCaptor<UploadHistory> captor = ArgumentCaptor.forClass(UploadHistory.class);
        verify(uploadHistoryService).save(captor.capture());
        UploadHistory saved = captor.getValue();
        Assertions.assertEquals(UploadStatus.FAILED, saved.getUploadStatus());
        verify(leadService, never()).bulkUpload(anyList());
    }

    // ---------- getLeadsByUserEmail ----------
    @Test
    void getLeadsByUserEmail_ShouldReturnDtos_WhenFound() {
        String email = "owner@x.com";
        User user = new User();
        user.setEmail(email);

        Lead l = new Lead();
        Product p = new Product();
        p.setProductName("P1");
        l.setInterestedProducts(Set.of(p));

        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));
        when(leadService.getLeadsByUser(user)).thenReturn(Optional.of(List.of(l)));

        List<LeadDto> result = leadHandler.getLeadsByUserEmail(email);

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).getInterestedModules().contains("P1"));
        verify(userService).getUserByEmail(email);
        verify(leadService).getLeadsByUser(user);
    }

    @Test
    void getLeadsByUserEmail_ShouldThrow_WhenUserNotFound() {
        String email = "no@x.com";
        when(userService.getUserByEmail(email)).thenReturn(Optional.empty());
        Assertions.assertThrows(UserException.class, () -> leadHandler.getLeadsByUserEmail(email));
    }

    @Test
    void getLeadsByUserEmail_ShouldThrow_WhenNoLeads() {
        String email = "noleads@x.com";
        User user = new User();
        user.setEmail(email);

        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));
        when(leadService.getLeadsByUser(user)).thenReturn(Optional.empty());

        Assertions.assertThrows(LeadException.class, () -> leadHandler.getLeadsByUserEmail(email));
    }

    // ---------- updateLeadStatus ----------
    @Test
    void updateLeadStatus_ShouldUpdateAndReturnNewStatus() {
        String email = "l@x.com";
        Lead lead = new Lead();
        lead.setId(100L);
        lead.setLeadStatus(LeadStatus.ADDED);
        when(leadService.getLeadByEmail(email)).thenReturn(Optional.of(lead));

        LeadStatus updated = leadHandler.updateLeadStatus(100L, 1); // index 1 -> next enum
        // Enum index 1 must be valid; ensure test maps correctly by comparing result and the lead
        Assertions.assertEquals(lead.getLeadStatus(), updated);
        verify(leadService).editLead(eq(lead.getId()), eq(lead));
    }

    @Test
    void updateLeadStatus_ShouldThrow_WhenLeadNotFound() {
        when(leadService.getLeadByEmail("missing")).thenReturn(Optional.empty());
        Assertions.assertThrows(LeadException.class, () -> leadHandler.updateLeadStatus(100L, 0));
    }
}
