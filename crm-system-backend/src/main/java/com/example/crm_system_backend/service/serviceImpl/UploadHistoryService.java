package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.UploadHistoryException;
import com.example.crm_system_backend.repository.IUploadHistoryRepository;
import com.example.crm_system_backend.service.IUploadHistoryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UploadHistoryService implements IUploadHistoryService {

    private static final Logger log = LoggerFactory.getLogger(UploadHistoryService.class);
    private final ModelMapper modelMapper;
    private IUploadHistoryRepository iUploadHistoryRepository;



    /**
     * Saves an UploadHistory entity into the database.
     * If the provided UploadHistory object is null, the method throws
     * an UploadHistoryException with the appropriate error code.
     *
     * @param uploadHistory the UploadHistory object to be saved
     * @return the saved UploadHistory object
     * @throws UploadHistoryException if the provided UploadHistory object is null
     * @throws IllegalArgumentException if the input is invalid for repository operations
     *
     * Author: Akshay Jadhav
     */
    @Override
    public UploadHistory save(UploadHistory uploadHistory) {
        try {
            log.info("Enter: ErrorRecordHandler.save");
            return iUploadHistoryRepository.save(uploadHistory);
        }
        catch (IllegalArgumentException e){
            throw new UploadHistoryException(ErrorCode.NULL_ARGUMENT_RECEIVED);
        }
    }

    /**
     * Retrieves an UploadHistory entity based on the given identifier.
     * If no matching record is found, an exception is thrown indicating
     * that the file history could not be found for the provided identifier.
     *
     * @param id the unique identifier of the UploadHistory entity
     * @return the UploadHistory entity corresponding to the given identifier
     * @throws ExcelException if no UploadHistory entry is found for the specified identifier
     *
     * Author: Akshay Jadhav
     */
    @Override
    public UploadHistory findById(String id) {
        log.info("Enter: ErrorRecordHandler.findById");
     UploadHistory uploadHistory =    iUploadHistoryRepository.findById(id).orElseThrow(
             ()-> {
                 log.error("Exception: ErrorRecordHandler.findById -->history not found for id {}",id);
                return new ExcelException(ErrorCode.FILE_HISTORY_NOT_FOUND);}
     );
        log.info("Exit: ErrorRecordHandler.findById");
        return uploadHistory;
    }

    /**
     * Updates an existing UploadHistory entity in the database. The method
     * retrieves the existing entity using its ID, maps the updated fields
     * from the new entity to the existing one, and saves the updated entity.
     * If the entity with the provided ID is not found, the method throws an exception.
     *
     * @param uploadHistory the UploadHistory object containing updated information
     * @return the updated UploadHistory object after saving to the database
     * @throws ExcelException if the UploadHistory entity with the given ID is not found
     *
     * Author: Akshay Jadhav
     */
    @Override
    public UploadHistory update(UploadHistory uploadHistory) {
        log.info("Enter: ErrorRecordHandler.update");
         UploadHistory savedUploadHistory = iUploadHistoryRepository.findById(uploadHistory.getId()).orElseThrow(
                 ()-> new ExcelException(ErrorCode.FILE_HISTORY_NOT_FOUND)
         );
         modelMapper.map(uploadHistory,savedUploadHistory);
        return iUploadHistoryRepository.save(savedUploadHistory);
    }

    /**
     * Deletes an UploadHistory entity identified by the provided ID.
     * If the ID is not found in the database, the method fails silently.
     *
     * @param id the unique identifier of the UploadHistory entity to be deleted
     */
    @Override
    public void deleteById(String id) {
        log.info("Enter: ErrorRecordHandler.deleteById");
        iUploadHistoryRepository.deleteById(id);
        log.info("Exit: ErrorRecordHandler.deleteById");
    }

    /**
     * Retrieves a list of UploadHistory entities associated with a specific user based on their email.
     * If no records are found, it throws an ExcelException with the appropriate error code.
     *
     * @param email the email address of the user whose upload history is being retrieved
     * @return a list of UploadHistory entities associated with the specified user
     * @throws ExcelException if no upload history is found for the provided email
     *
     * Author: Akshay Jadhav
     */
    @Override
    public List<UploadHistory> findByUser(String email) {
        log.info("Enter: ErrorRecordHandler.findByUser");
        return iUploadHistoryRepository.findByUploadedBy(email).orElseThrow(
                ()-> {
                    log.error("Exception: ErrorRecordHandler.findByUser -->history not found for email {}",email);
                   return new ExcelException(ErrorCode.FILE_HISTORY_NOT_FOUND);
                }
        );
    }
}
