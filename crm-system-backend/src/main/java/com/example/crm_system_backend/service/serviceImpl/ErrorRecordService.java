package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.entity.ErrorRecord;
import com.example.crm_system_backend.repository.ErrorRecordRepository;
import com.example.crm_system_backend.service.IErrorRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ErrorRecordService implements IErrorRecordService {

    private  final ErrorRecordRepository errorRecordRepository;


    public ErrorRecordService(ErrorRecordRepository errorRecordRepository) {
        this.errorRecordRepository = errorRecordRepository;
    }


    public ErrorRecord saveErrorRecord(ErrorRecord errorRecord){
        return errorRecordRepository.save(errorRecord);
    }

    public List<ErrorRecord> findAllErrorRecords(){
        return errorRecordRepository.findAll();
    }

    public Optional<ErrorRecord> findErrorRecordByUploadHistoryId(String id){
        return errorRecordRepository.findByUploadHistoryId(id);
    }

    public Optional<ErrorRecord> findErrorRecordById(String id){
      return  errorRecordRepository.findErrorRecordById(id);
    }
}
