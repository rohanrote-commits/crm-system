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

    @Override
    public UploadHistory update(UploadHistory uploadHistory) {
        log.info("Enter: ErrorRecordHandler.update");
         UploadHistory savedUploadHistory = iUploadHistoryRepository.findById(uploadHistory.getId()).orElseThrow(
                 ()-> new ExcelException(ErrorCode.FILE_HISTORY_NOT_FOUND)
         );
         modelMapper.map(uploadHistory,savedUploadHistory);
        return iUploadHistoryRepository.save(savedUploadHistory);
    }

    @Override
    public void deleteById(String id) {
        log.info("Enter: ErrorRecordHandler.deleteById");
        iUploadHistoryRepository.deleteById(id);
        log.info("Exit: ErrorRecordHandler.deleteById");
    }

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
