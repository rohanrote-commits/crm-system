package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.Product;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.repository.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

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


    @Test
    void saveLeadSuccess() {

        // Arrange
        LeadDto leadDto = new LeadDto();
        leadDto.setFirstName("John");
        leadDto.setLastName("Doe");
        leadDto.setEmail("akashy9090@gmail.com");
        leadDto.setGstin("29AACT1990PCE1ZX");
        leadDto.setLeadStatus(LeadStatus.ADDED);
        leadDto.setBusinessAddress("Bangalore");
        leadDto.setDescription("I am interested in CRM");
        leadDto.setUser("akshay@gmail.com");
        leadDto.setInterestedModules(Set.of("CRM"));

        User mockUser = new User();
        mockUser.setId(Mockito.anyLong());
        mockUser.setEmail("akshay@gmail.com");


        Product mockProduct = new Product();
        mockProduct.setId(10L);
        mockProduct.setModuleName("CRM");

        Lead mockSavedLead = new Lead();
        mockSavedLead.setFirstName("John");
        mockSavedLead.setEmail("akashy9090@gmail.com");

        // repository stubs
        Mockito.when(userRepo.getUserByEmail(anyString()))
                .thenReturn(Optional.of(mockUser));

        Mockito.when(leadRepository.getLeadsByEmail(anyString()))
                .thenReturn(Optional.empty()); // new lead

        Mockito.when(productRepo.getProductByModuleName(anyString()))
                .thenReturn(Optional.of(mockProduct));

        Mockito.when(leadRepository.save(Mockito.any(Lead.class)))
                .thenReturn(mockSavedLead);

        // Act
        Lead result = leadService.save(leadDto);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("akashy9090@gmail.com", result.getEmail());
    }

    @Test
    void getLeadsByUser() {
    }

    @Test
    void getAllLeads() {
    }

    @Test
    void editLead() {
    }

    @Test
    void deleteLead() {
    }

    @Test
    void bulkUpload() {
    }

    @Test
    void getLeadById() {
    }

    @Test
    void getLeadByEmail() {
    }

    @Test
    void findByUserIn() {
    }
}