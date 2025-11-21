package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.LeadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.Product;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ProductException;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.repository.ProductRepo;
import com.example.crm_system_backend.service.ILeadService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class LeadService implements ILeadService {

    private static final Logger log = LoggerFactory.getLogger(LeadService.class);
    private final ILeadRepository leadRepository;
    private final IUserRepo userRepo;
    private final ModelMapper modelMapper;
    private final ProductRepo productRepo;



    @Override
    public Lead save(LeadDto leadDto) {
        log.info("Enter: LeadService.save ");
        User user = userRepo.getUserByEmail(leadDto.getUser()).orElseThrow(
                ()-> {
                    log.error("Exception: LeadService.save---> user not found");
                   return new UserException(ErrorCode.USER_NOT_FOUND);
                }
        );
        Lead lead = modelMapper.map(leadDto, Lead.class);
        lead.setUser(user);
        lead.setGstin(leadDto.getGstin().toUpperCase());
        lead.setCreatedAt(new Date());
        lead.setUpdatedAt(new Date());
        lead.setLeadStatus(LeadStatus.ADDED);
          Set<Product> productSet =  leadDto.getInterestedModules().stream().map(
                    interestedModule -> productRepo.getProductByModuleName(interestedModule).orElseThrow(
                            ()-> {
                                log.error("Exception: LeadService.save---> product not found");
                               return new ProductException(ErrorCode.PRODUCT_NOT_FOUND);}
                    )
            ).collect(Collectors.toSet());
          lead.setInterestedProducts(productSet);
          log.info("Exit: LeadService.save");
       return leadRepository.save(lead);
    }

    @Override
    public Optional<List<Lead>> getLeadsByUser(User user) {
        log.info("Enter: LeadService.getLeadsByUser");
        return leadRepository.getLeadsByUser(user);
    }

    @Override
    public List<Lead> getAllLeads() {
        log.info("Enter: LeadService.getAllLeads");
       return leadRepository.findAll();
    }

    @Override
    public Lead editLead(Long leadId,Lead lead) {
        log.info("Enter: LeadService.editLead");
       return leadRepository.save(lead);
    }

    @Override
    public void deleteLead(Long leadId) {
        log.info("Enter: LeadService.deleteLead");
        leadRepository.deleteById(Math.toIntExact(leadId));
    }

    @Override
    public List<Lead> bulkUpload(List<Lead> leads) {
        log.info("Enter: LeadService.bulkUpload");
      return   leadRepository.saveAll(leads);
    }

    public Optional<Lead> getLeadById(Long leadId) {
        log.info("Enter: LeadService.getLeadById");
        return leadRepository.getLeadsById(leadId);
    }

    public Optional<Lead> getLeadByEmail(String email) {
        log.info("Enter: LeadService.getLeadByEmail");
        return leadRepository.getLeadsByEmail(email);
    }

    public List<Lead> findByUserIn(List<User> allUsers) {
        log.info("Enter: LeadService.findByUserIn");
        return  leadRepository.findByUserIn(allUsers);
    }
}
