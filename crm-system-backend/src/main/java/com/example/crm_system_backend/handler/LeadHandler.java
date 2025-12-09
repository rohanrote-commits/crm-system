package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.LeadList;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.FileTemplateType;
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
import com.example.crm_system_backend.service.ILeadService;
import com.example.crm_system_backend.service.serviceImpl.LeadService;
import com.example.crm_system_backend.service.serviceImpl.ProductService;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
public class LeadHandler implements IHandler<LeadDto> {

    private static final Logger log = LoggerFactory.getLogger(LeadHandler.class);
    private final LeadService leadService;
    private final UserService userService;

    private final LeadExcelHelper  leadExcelHelper;

    private final ModelMapper modelMapper;

    private final UploadHistoryService uploadHistoryService;

    private final ErrorRecordHandler errorRecordHandler;

    private final ProductService productService;
    private final ILeadService iLeadService;


    /**
     * Saves a lead into the system after performing necessary validations, such as
     * checking if a lead with the same email already exists. If the lead exists, an exception
     * is thrown. Otherwise, the lead is saved and mapped to a LeadDto object.
     *
     * @param leadDto the lead data to be saved, encapsulated in a {@link LeadDto} object
     * @return the saved lead data represented as a {@link LeadDto} object
     * @throws LeadException if a lead with the given email already exists
     */
    @Override
    public LeadDto save(LeadDto leadDto) {
        log.info("Enter: LeadHandler.save");
         leadService.getLeadByEmail(leadDto.getEmail()).ifPresent(
                 lead -> {
                     throw new LeadException(ErrorCode.LEAD_ALREADY_EXISTS);
                 }
         );

        Lead savedLead =  leadService.save(leadDto);
        return modelMapper.map(savedLead,LeadDto.class);
    }


    /**
     * Retrieves a list of leads associated with a specific user and any sub-users registered by that user.
     * If the user is not found, a {@link UserException} is thrown.
     *
     * @param userId the unique ID of the user whose leads are to be fetched
     * @return a list of {@link LeadDto} objects representing the leads associated with the user
     * @throws UserException if the user with the specified ID does not exist
     * @author Akshay Jadhav
     */
    public List<LeadDto> getLeadsByUser(Long userId) {
        log.info("Enter: LeadHandler.getLeadsByUser");
        User mainUser = userService.getUserById(userId)
                .orElseThrow(() -> {
                    log.error("LeadHandler.getLeadsByUser: User not found");
                    return new UserException(ErrorCode.USER_NOT_FOUND);
                });
        List<User> subUsers = userService.getAllUsersRegisterById(userId)
                .orElse(new ArrayList<>());
        List<User> allUsers = new ArrayList<>();
        allUsers.add(mainUser);
        allUsers.addAll(subUsers);

        //Fetch all leads for all these users (ONE DB CALL)
        List<Lead> leads = leadService.findByUserIn(allUsers);
        log.info("Exit: LeadHandler.getLeadsByUser");
        List<LeadDto> leadDtoList = leads.stream()
                .map(lead -> {
                            LeadDto leadDto = new LeadDto();
                            //Converting Product -> productName
                            Set<String> products = lead.getInterestedProducts().stream().map(
                                    Product::getProductName
                            ).collect(Collectors.toSet());
                            modelMapper.map(lead, leadDto);
                            leadDto.setInterestedModules(products);
                            return leadDto;
                        }
                )
                .toList();

        return  leadDtoList;
    }

    /**
     * Retrieves a Lead entity based on the provided email address.
     * If no lead is found with the given email, a {@link LeadException} is thrown
     * with an appropriate error code.
     *
     * @param email the email address used to look up the lead
     * @return the Lead entity associated with the specified email
     * @throws LeadException if no lead is found with the provided email address
     * @author Akshay Jadhav
     */
    public Lead getLeadByEmail(String email){
        log.info("Enter: LeadHandler.getLeadByEmail");
        return leadService.getLeadByEmail(email).orElseThrow(
                ()-> {
                    log.error("Exit: LeadHandler.getLeadByEmail: Lead not found");
                   throw  new LeadException(ErrorCode.LEAD_NOT_FOUND);
                }
        );
    }

    /**
     * Retrieves a list of all leads available in the system.
     * Each lead is mapped to a {@link LeadDto} object, including data transformations
     * such as converting products to their corresponding product names.
     *
     * @return a list of {@link LeadDto} objects, each representing a lead along
     *         with its associated details and transformed interested modules (products).
     * @author Akshay Jadhav
     */
    @Override
    public List<LeadDto> getAll() {
        log.info("Enter: LeadHandler.getAll");
      List<LeadDto> leadList =  leadService.getAllLeads().stream().map(
              lead -> {
                  LeadDto leadDto = new LeadDto();
                  //Converting Product -> productName
                  Set<String> products = lead.getInterestedProducts().stream().map(
                          Product::getProductName
                  ).collect(Collectors.toSet());
                  modelMapper.map(lead, leadDto);
                  leadDto.setInterestedModules(products);
                  return leadDto;
        }).toList();
      log.info("Exit: LeadHandler.getAll");
        return leadList;
    }

    /**
     * Edits an existing lead by updating its properties with the provided data.
     * Performs validation to ensure the lead exists, and updates fields such as
     * interested modules and timestamps.
     *
     * @param leadId the ID of the lead to be updated
     * @param leadDto the new data for updating the lead, encapsulated in a {@link LeadDto} object
     * @return the updated lead information represented as a {@link LeadDto} object
     * @throws LeadException if the lead with the specified ID does not exist
     * @author Akshay Jadhav
     */
    @Override
    public LeadDto edit(Long leadId, LeadDto leadDto) {
        log.info("Enter: LeadHandler.edit");
        Lead oldLead = leadService.getLeadById(leadId).orElseThrow(
                ()-> {
                    log.error("Exit: LeadHandler.edit->lead not found");
                    return new LeadException(ErrorCode.LEAD_NOT_FOUND);
                }
        );
        modelMapper.map(leadDto, oldLead);
        oldLead.setId(leadId);
        oldLead.setUpdatedAt(new Date());
        Set<Product> productSet =  leadDto.getInterestedModules().stream().map(
                productService::getProductByName
        ).collect(Collectors.toSet());
        oldLead.setInterestedProducts(productSet);
        leadService.editLead(leadId,oldLead);
        log.info("Exit: LeadHandler.edit");
        return  modelMapper.map(oldLead,LeadDto.class);
    }

    /**
     * Deletes a lead identified by the provided lead ID. This method delegates the operation
     * to the {@code leadService} to perform the deletion from the database. Logs are captured
     * before and after the deletion operation for tracking purposes.
     *
     * @param leadId the unique identifier of the lead to be deleted
     */
    @Override
    public void delete(Long leadId) {
        log.info("Enter: LeadHandler.delete");
        leadService.deleteLead(leadId);
        log.info("Exit: LeadHandler.delete");
    }

    /**
     * Handles the bulk upload of a file containing lead data. This method processes the file,
     * validates its content, saves valid leads to the database, and updates the upload history status
     * accordingly.
     *
     * @param file   the file to be uploaded and processed, encapsulated as a {@link MultipartFile} object
     * @param userId the unique identifier of the user performing the bulk upload
     * @throws UserException     if the user with the specified ID does not exist
     * @throws LeadException     if an error occurs while processing the file
     * @throws RuntimeException  if any other unexpected error occurs during the upload process
     * @author Akshay Jadhav
     */
//    @Override
//    public void bulkUpload(MultipartFile file, Long userId) {
//        log.info("Enter: LeadHandler.bulkUpload");
//
//        if (!file.isEmpty()) {
//            String fileName = file.getOriginalFilename();
//            String uploadHistoryId  = fileName.substring(11 ,fileName.lastIndexOf("."));
//            if (uploadHistoryId.length()>36){
//                uploadHistoryId = uploadHistoryId.substring(0,36);
//             UploadHistory uploadHistory =  uploadHistoryService.findById(uploadHistoryId);
//             if (uploadHistory!=null){
//                 try {
//                     LeadList leadList = leadExcelHelper.processExcelData(file, uploadHistory).get();
//
//                 } catch (InterruptedException e) {
//                     throw new RuntimeException(e);
//                 }
//             }
//            }
//
//            return;
//        }
//
//        UploadHistory uploadHistory = new UploadHistory();
//        uploadHistory.setFileName(file.getOriginalFilename());
//        uploadHistory.setFileTemplateType(FileTemplateType.LEAD);
//        uploadHistory.setUploadedAt(LocalDateTime.now());
//        uploadHistory.setUploadStatus(UploadStatus.PROCESSING);
//
//        try {
//            // Validate User
//            User user = userService.getUserById(userId).orElseThrow(
//                    () -> new UserException(ErrorCode.USER_NOT_FOUND)
//            );
//            uploadHistory.setUploadedBy(user.getEmail());
//            // Process Excel
//            LeadList leadList = leadExcelHelper.processExcelData(file, uploadHistory).get();
//            List<Lead> validLeadList = leadList.getValidLeadList();
//            List<Lead> invalidLeadList = leadList.getInvalidLeadList();
//            // Save valid data
//            if (!validLeadList.isEmpty()) {
//                validLeadList.forEach(lead -> {
//                    lead.setCreatedAt(new Date());
//                    lead.setUpdatedAt(new Date());
//                    lead.setLeadStatus(LeadStatus.ADDED);
//                    lead.setUser(user);
//                });
//
//                leadService.bulkUpload(validLeadList);
//            }
//            // ------ Set Status ------
//            if (!validLeadList.isEmpty() && !invalidLeadList.isEmpty()) {
//                uploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
//            }
//            else if (!validLeadList.isEmpty()) {
//                uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
//            }
//            else if (!invalidLeadList.isEmpty()) {
//                uploadHistory.setUploadStatus(UploadStatus.FAILED);
//            }
//            else {
//                uploadHistory.setUploadStatus(UploadStatus.FAILED); // empty file or unexpected
//            }
//            uploadHistoryService.save(uploadHistory);
//        }
//        catch (Exception e) {
//            log.error("Exit: LeadHandler.bulkUpload Exception:", e);
//            uploadHistory.setUploadStatus(UploadStatus.FAILED);
//            uploadHistory.setUploadedAt(LocalDateTime.now());
//            uploadHistoryService.save(uploadHistory);
//            throw new LeadException(ErrorCode.FILE_PROCESSING_EXCEPTION);
//        }
//
//        log.info("Exit: LeadHandler.bulkUpload");
//    }

    @Override
    @Transactional
    public void bulkUpload(MultipartFile file, Long userId) {

        log.info("Enter: LeadHandler.bulkUpload");
        if (file.isEmpty()) {
            throw new LeadException(ErrorCode.FILE_EMPTY_EXCEPTION);
        }

        try {
            String fileName = file.getOriginalFilename();
            String uploadHistoryId = null;
            if(fileName !=null && !fileName.contains("Lead_Teamplate")){
                uploadHistoryId   = extractHistoryIdFromFilename(fileName);
            }
            UploadHistory uploadHistory;
            if (uploadHistoryId != null) {
                // Correction Upload Flow
                uploadHistory = this.handleCorrectionUpload(uploadHistoryId);
            } else {
                // Fresh Upload Flow
                uploadHistory = this.createNewUploadHistory(fileName, userId);
            }
            // Process Valid & Invalid Leads
            LeadList leadList = leadExcelHelper.processExcelData(file, uploadHistory).get();
            this.saveValidLeads(leadList.getValidLeadList(), userId);
            this.updateUploadStatus(uploadHistory, leadList);

            log.info("Exit: LeadHandler.bulkUpload");
        }
        catch (Exception ex) {
            log.error("Error during bulk upload:", ex);
            throw new LeadException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }

    private String extractHistoryIdFromFilename(String fileName) {
        if (fileName == null || !fileName.contains(".")) return null;

        String uploadHistoryId  = fileName.substring(11 ,fileName.lastIndexOf("."));
            if (uploadHistoryId.length()>36) {
                uploadHistoryId = uploadHistoryId.substring(0, 36);
            }
        return uploadHistoryId;
    }

    private UploadHistory handleCorrectionUpload(String uploadHistoryId) {
        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);

        if (uploadHistory == null) {
            throw new LeadException(ErrorCode.UPLOAD_HISTORY_NOT_FOUND);
        }
        // Delete previous invalid data (error records)
        uploadHistoryService.deleteErrorRecords(uploadHistoryId);
        uploadHistory.setUploadedAt(LocalDateTime.now());
        uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
        uploadHistoryService.save(uploadHistory);
        log.info("Correction upload: Previous invalid records removed");
        return uploadHistory;
    }

    private UploadHistory createNewUploadHistory(String fileName, Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        UploadHistory uploadHistory = new UploadHistory();
        uploadHistory.setFileName(fileName);
        uploadHistory.setFileTemplateType(FileTemplateType.LEAD);
        uploadHistory.setUploadedBy(user.getEmail());
        uploadHistory.setUploadedAt(LocalDateTime.now());
        uploadHistory.setUploadStatus(UploadStatus.PROCESSING);

        return uploadHistoryService.save(uploadHistory);
    }

    private void saveValidLeads(List<Lead> validLeadList, Long userId) {
        if (validLeadList.isEmpty()) return;
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        validLeadList.forEach(lead -> {
            lead.setCreatedAt(new Date());
            lead.setUpdatedAt(new Date());
            lead.setLeadStatus(LeadStatus.ADDED);
            lead.setUser(user);
        });

        leadService.bulkUpload(validLeadList);
    }

    private void updateUploadStatus(UploadHistory uploadHistory, LeadList leadList) {

        if (!leadList.getValidLeadList().isEmpty() && ! leadList.getInvalidLeadList().isEmpty()) {
                uploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
            }
            else if (!leadList.getValidLeadList().isEmpty()) {
                uploadHistory.setUploadStatus(UploadStatus.SUCCESS);
            }
            else if (!leadList.getInvalidLeadList().isEmpty()) {
                uploadHistory.setUploadStatus(UploadStatus.FAILED);
            }
            else {
                uploadHistory.setUploadStatus(UploadStatus.FAILED); // empty file or unexpected
            }

        uploadHistoryService.save(uploadHistory);
    }

    /**
     * Retrieves a list of leads associated with the user identified by the provided email address.
     * If the user is not found, a {@link UserException} with an error code {@code USER_NOT_FOUND} is thrown.
     * Additionally, if no leads are associated with the user, a {@link LeadException} with an error code
     * {@code LEAD_NOT_FOUND} is thrown.
     *
     * @param email the email address of the user whose leads are to be retrieved
     * @return a list of {@link LeadDto} objects representing the leads associated with the user
     * @throws UserException if no user is found with the provided email address
     * @throws LeadException if no leads are associated with the user identified by the email
     * @author Akshay Jadhav
     */
    public List<LeadDto> getLeadsByUserEmail(String email) {
        log.info("Enter: LeadHandler.getLeadsByUserEmail");
        User user = userService.getUserByEmail(email).orElseThrow(

                ()-> {
                    log.error("Exit: LeadHandler.getLeadsByUserEmail -> User not found");
                   return new UserException(ErrorCode.USER_NOT_FOUND);
                }
        );
        List<LeadDto> leadList =  leadService.getLeadsByUser(user).orElseThrow(
                ()-> new LeadException(ErrorCode.LEAD_NOT_FOUND)
        ).stream().map(lead -> {
            LeadDto leadDto = new LeadDto();
            //Converting Product -> productName
           Set<String> products = lead.getInterestedProducts().stream().map(
                   Product::getProductName
            ).collect(Collectors.toSet());
            modelMapper.map(lead, leadDto);
            leadDto.setInterestedModules(products);
            return leadDto;
        }).toList();
        log.info("Exit: LeadHandler.getLeadsByUserEmail");
        return leadList;
    }


    /**
     * Converts a {@link Product} entity to its corresponding product name.
     *
     * This method is intended to extract and return the name of the product
     * represented by the provided {@link Product} entity. The product name is
     * expected to be a unique and non-null field within the {@link Product} entity.
     *
     * @param product the {@link Product} entity whose name is to be retrieved
     * @return the name of the product as a {@link String}, or an empty string if
     *         the product name cannot be retrieved or is null
     * @author Akshay Jadhav
     */
    private String ProductEntityToItsName(Product product) {
        return  "";
    }

    /**
     * Updates the status of a lead based on the provided lead ID and status value.
     * If the lead with the given ID does not exist, an exception is thrown.
     * The status is determined by mapping the provided integer value to a corresponding
     * {@link LeadStatus} enumeration.
     *
     * @param id the unique identifier of the lead whose status is to be updated
     * @param status an integer representing the new status of the lead,
     *               which corresponds to the ordinal values of {@link LeadStatus}
     * @return the updated status of the lead as a {@link LeadStatus} enumeration
     * @throws LeadException if the lead with the specified ID is not found
     * @author Akshay Jadhav
     */
    public LeadStatus updateLeadStatus(Long id,int status) {
        log.info("Enter: LeadHandler.updateLeadStatus");
        Lead lead =  leadService.getLeadById(id).orElseThrow(
                ()-> {
                    log.error("Exception: LeadHandler.updateLeadStatus -> Lead not found");
                    return new  LeadException(ErrorCode.LEAD_NOT_FOUND);
                }
        );
        LeadStatus leadStatus = LeadStatus.values()[status];
        lead.setLeadStatus(leadStatus);
        leadService.editLead(lead.getId(), lead);
        log.info("Exit: LeadHandler.updateLeadStatus");
        return lead.getLeadStatus();
    }
}
