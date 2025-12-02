package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.Product;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ProductException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.repository.ProductRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;



@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private ILeadRepository leadRepository;

    @InjectMocks
    private LeadService leadService;

    @Mock
    IUserRepo userRepo;

    @Mock
    ProductRepo productRepo;

    @Mock
    ModelMapper modelMapper;

    LeadDto dto;

    @BeforeEach
    public void getMockLead(){
        dto = new LeadDto();
        dto.setEmail("john@example.com");
        dto.setUser("akshay@gmail.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setGstin("abc123");
        dto.setLeadStatus(LeadStatus.ADDED);
        dto.setBusinessAddress("Pune");
        dto.setDescription("Test Lead");
        dto.setInterestedModules(Set.of("CRM"));
        dto.setId(1L);
    }

    @Test
    void saveLeadSuccess() {

        // -------- Arrange --------


        User mockUser = new User();
        mockUser.setEmail("akshay@gmail.com");

        Product mockProduct = new Product();
        mockProduct.setProductName("CRM");

        Lead mappedLead = new Lead();

        Mockito.when(userRepo.getUserByEmail("akshay@gmail.com"))
                .thenReturn(Optional.of(mockUser));

        Mockito.when(leadRepository.getLeadsByEmail("john@example.com"))
                .thenReturn(Optional.empty());

        Mockito.when(productRepo.getProductByProductName("CRM"))
                .thenReturn(Optional.of(mockProduct));

        Mockito.when(modelMapper.map(dto, Lead.class))
                .thenReturn(mappedLead);

        Mockito.when(leadRepository.save(Mockito.any(Lead.class)))
                .thenAnswer(inv -> inv.getArgument(0));


        Lead result = leadService.save(dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockUser, result.getUser());
        Assertions.assertTrue(result.getInterestedProducts().contains(mockProduct));

        Mockito.verify(leadRepository).save(result);
    }

    @Test
    void saveLeadUserNotFound_ShouldThrowException() {

        Mockito.when(userRepo.getUserByEmail(dto.getUser()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(UserException.class, () -> leadService.save(dto));

        Mockito.verify(userRepo).getUserByEmail(dto.getUser());
        Mockito.verifyNoInteractions(productRepo, leadRepository, modelMapper);
    }

    @Test
    void saveLeadProductNotFound_ShouldThrowException() {


        // -------- Arrange --------
        User mockUser = new User();
        mockUser.setEmail("akshay@gmail.com");

        Mockito.when(userRepo.getUserByEmail("akshay@gmail.com"))
                .thenReturn(Optional.of(mockUser));

        Mockito.when(leadRepository.getLeadsByEmail("john@example.com"))
                .thenReturn(Optional.empty());

        Mockito.when(modelMapper.map(dto, Lead.class))
                .thenReturn(new Lead());

        Mockito.when(productRepo.getProductByProductName("CRM"))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ProductException.class, () -> leadService.save(dto));

        Mockito.verify(userRepo).getUserByEmail(dto.getUser());
        Mockito.verify(productRepo).getProductByProductName("CRM");
//        Mockito.verifyNoInteractions(leadRepository);
    }

    @Test
    void getLeadsByUser() {

        User mockUser = new User();
        mockUser.setFirstName("Aman");
        mockUser.setEmail("test@gmmail.com");
        mockUser.setId(1L);
        mockUser.setRole(Roles.ADMIN);
        mockUser.setRegisteredBy(1L);
        mockUser.setMobileNumber("8123456723");

        Lead mocklead = new Lead();
        mocklead.setUser(mockUser);
        mocklead.setFirstName("Akshay");
        mocklead.setLastName("Jadhav");
        mocklead.setId(2L);
        mocklead.setGstin("ABCKESCDTRWBTYU");
        mocklead.setEmail("lead@gmail.com");

        Mockito.when(leadRepository.getLeadsByUser(mockUser)).thenReturn(Optional.of(List.of(mocklead)));

        List<Lead> leadList = leadService.getLeadsByUser(mockUser).get();

        Assertions.assertNotNull(leadList);
        Assertions.assertEquals(1, leadList.size());
        Assertions.assertEquals(mocklead, leadList.get(0));
        Assertions.assertNotNull(leadList);
        Assertions.assertEquals("lead@gmail.com", leadList.get(0).getEmail());

    }

    @Test
    void getAllLeads() {

        Lead lead1 = new Lead();
        lead1.setId(1L);

        Lead lead2 = new Lead();
        lead2.setId(2L);

        List<Lead> leads = List.of(lead1, lead2);

        Mockito.when(leadRepository.findAll()).thenReturn(leads);

        List<Lead> result = leadService.getAllLeads();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(leads, result);
        Mockito.verify(leadRepository, Mockito.times(1)).findAll();
    }

    @Test
    void getAllLeads_ShouldReturnEmptyList_WhenNoLeadsExist() {

        Mockito.when(leadRepository.findAll()).thenReturn(Collections.emptyList());

        List<Lead> result = leadService.getAllLeads();

        Assertions.assertTrue(result.isEmpty());
        Mockito.verify(leadRepository).findAll();
    }

    @Test
    void editLead() {

        Lead lead = new Lead();
        lead.setEmail("sam@gmail.com");
        lead.setGstin("ABCDEGGHIJKLMNOP");
        lead.setInterestedProducts(Set.of(new Product()));
        lead.setLeadStatus(LeadStatus.CONTACTED);
        lead.setFirstName("Sam");
        lead.setLastName("Jadhav");
        lead.setId(1L);

        Mockito.when(leadRepository.save(Mockito.any(Lead.class)))
                .thenReturn(lead);

        leadService.editLead(1L, lead);

        Mockito.verify(leadRepository, Mockito.times(1)).save(lead);

    }

    @Test
    void deleteLead() {
        Long leadId = 1L;
        leadService.deleteLead(leadId);
        Mockito.verify(leadRepository).deleteById(Math.toIntExact(leadId));

    }

    @Test
    void bulkUpload() {
        Lead lead1 = new Lead();
        Lead lead2 = new Lead();
        List<Lead> leads = List.of(lead1, lead2);

        Mockito.when(leadRepository.saveAll(leads)).thenReturn(leads);

        List<Lead> result = leadService.bulkUpload(leads);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Mockito.verify(leadRepository).saveAll(leads);
    }

    @Test
    void getLeadById() {
        Long leadId = 10L;
        Lead lead = new Lead();
        lead.setId(leadId);

        Mockito.when(leadRepository.getLeadsById(leadId)).thenReturn(Optional.of(lead));

        Optional<Lead> result = leadService.getLeadById(leadId);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(leadId, result.get().getId());
        Mockito.verify(leadRepository).getLeadsById(leadId);
    }

    @Test
    void getLeadByEmail() {
        String email = "test@example.com";
        Lead lead = new Lead();
        lead.setEmail(email);

        Mockito.when(leadRepository.getLeadsByEmail(email)).thenReturn(Optional.of(lead));

        Optional<Lead> result = leadService.getLeadByEmail(email);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(email, result.get().getEmail());
        Mockito.verify(leadRepository).getLeadsByEmail(email);
    }

    @Test
    void findByUserIn() {
        User user1 = new User();
        user1.setId(1L);
        List<User> users = List.of(user1);

        Lead lead = new Lead();
        List<Lead> leads = List.of(lead);

        Mockito.when(leadRepository.findByUserIn(users)).thenReturn(leads);

        List<Lead> result = leadService.findByUserIn(users);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Mockito.verify(leadRepository).findByUserIn(users);
    }
}