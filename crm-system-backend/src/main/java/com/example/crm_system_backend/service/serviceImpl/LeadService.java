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



    /**
     * Saves or updates a lead based on the provided LeadDto. If a lead with the specified email exists,
     * it updates the existing lead; otherwise, it creates a new lead.
     *
     * @param leadDto the lead data transfer object containing lead information to be saved
     * @return the saved or updated Lead entity
     */
    @Override
    public Lead save(LeadDto leadDto) {

        log.info("Enter: LeadService.save");

        User user = userRepo.getUserByEmail(leadDto.getUser())
                .orElseThrow(() -> {
                    log.error("Exception: LeadService.save -> user not found");
                    return new UserException(ErrorCode.USER_NOT_FOUND);
                });

        Optional<Lead> existingLeadOpt = leadRepository.getLeadsByEmail(leadDto.getEmail());

        Lead lead;

        if (existingLeadOpt.isPresent()) {
            // Update existing lead
            lead = existingLeadOpt.get();
            // Map updatable fields only
            lead.setFirstName (leadDto.getFirstName());
            lead.setLastName(leadDto.getLastName());
            lead.setGstin(leadDto.getGstin().toUpperCase());
            lead.setUpdatedAt(new Date());
            lead.setLeadStatus(leadDto.getLeadStatus());
            lead.setBusinessAddress(leadDto.getBusinessAddress());
            lead.setDescription(leadDto.getDescription());
        } else {
            // Create new lead
            lead = modelMapper.map(leadDto, Lead.class);
            lead.setCreatedAt(new Date());
            lead.setUpdatedAt(new Date());
            lead.setLeadStatus(LeadStatus.ADDED);
        }

        // Common fields for both create & update
        lead.setUser(user);

        // Modules → Products
        Set<Product> productSet = leadDto.getInterestedModules().stream()
                .map(module -> productRepo.getProductByProductName(module)
                        .orElseThrow(() -> {
                            log.error("Exception: LeadService.save -> product not found");
                            return new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
                        }))
                .collect(Collectors.toSet());

        lead.setInterestedProducts(productSet);

        log.info("Exit: LeadService.save");

        return leadRepository.save(lead);
    }

    /**
     * Retrieves a list of leads associated with the specified user.
     *
     * @param user the user whose leads are to be fetched
     * @return an Optional containing a list of leads associated with the given user,
     *         or an empty Optional if no leads are found
     * @author Akshay Jadhav
     */
    @Override
    public Optional<List<Lead>> getLeadsByUser(User user) {
        log.info("Enter: LeadService.getLeadsByUser");
        return leadRepository.getLeadsByUser(user);
    }

    /**
     * Retrieves all leads from the repository.
     *
     * @return a list of all leads available in the repository
     */
    @Override
    public List<Lead> getAllLeads() {
        log.info("Enter: LeadService.getAllLeads");
       return leadRepository.findAll();
    }

    /**
     * Updates an existing lead associated with the specified ID by saving the provided Lead object.
     *
     * @param leadId The unique identifier of the lead to be edited.
     * @param lead   The Lead object containing updated information.
     */
    @Override
    public void editLead(Long leadId,Lead lead) {
        log.info("Enter: LeadService.editLead");
       leadRepository.save(lead);
    }

    /**
     * Deletes a lead with the given ID from the system.
     *
     * @param leadId the ID of the lead to be deleted
     */
    @Override
    public void deleteLead(Long leadId) {
        log.info("Enter: LeadService.deleteLead");
        leadRepository.deleteById(Math.toIntExact(leadId));
    }

    /**
     * Uploads a bulk list of leads to the database.
     *
     * @param leads the list of Lead objects to be uploaded
     * @return a list of Lead objects that have been successfully saved
     */
    @Override
    public List<Lead> bulkUpload(List<Lead> leads) {
        log.info("Enter: LeadService.bulkUpload");
      return   leadRepository.saveAll(leads);
    }

    /**
     * Retrieves a lead by its unique identifier.
     *
     * @param leadId the unique identifier of the lead to retrieve
     * @return an Optional containing the Lead if found, or an empty Optional if no lead exists with the given identifier
     * @author Akshay Jadhav
     */
    public Optional<Lead> getLeadById(Long leadId) {
        log.info("Enter: LeadService.getLeadById");
        return leadRepository.getLeadsById(leadId);
    }

    /**
     * Retrieves a Lead entity based on the provided email address.
     *
     * @param email The email address associated with the Lead to be retrieved.
     * @return An Optional containing the Lead if found, otherwise an empty Optional.
     * @author Akshay Jadhav
     */
    public Optional<Lead> getLeadByEmail(String email) {
        log.info("Enter: LeadService.getLeadByEmail");
        return leadRepository.getLeadsByEmail(email);
    }

    /**
     * Finds and retrieves a list of Lead entities associated with the given list of User entities.
     *
     * @param allUsers the list of User entities for whom the associated leads are to be retrieved
     * @return a list of Lead entities associated with the provided list of User entities
     */
    public List<Lead> findByUserIn(List<User> allUsers) {
        log.info("Enter: LeadService.findByUserIn");
        return  leadRepository.findByUserIn(allUsers);
    }
}
