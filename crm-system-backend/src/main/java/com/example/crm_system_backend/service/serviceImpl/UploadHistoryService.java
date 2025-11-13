package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.repository.IUploadHistoryRepository;
import com.example.crm_system_backend.service.IUploadHistoryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UploadHistoryService implements IUploadHistoryService {

    private final ModelMapper modelMapper;
    private IUploadHistoryRepository iUploadHistoryRepository;



    @Override
    public UploadHistory save(UploadHistory uploadHistory) {
      return    iUploadHistoryRepository.save(uploadHistory);
    }

    @Override
    public UploadHistory findById(String id) {
     UploadHistory uploadHistory =    iUploadHistoryRepository.findById(id).orElseThrow(
             ()-> new ExcelException(ErrorCode.FILE_HISTORY_NOT_FOUND)
     );
        return uploadHistory;
    }

    @Override
    public UploadHistory update(UploadHistory uploadHistory) {
         UploadHistory savedUploadHistory = iUploadHistoryRepository.findById(uploadHistory.getId()).orElseThrow(
                 ()-> new ExcelException(ErrorCode.FILE_HISTORY_NOT_FOUND)
         );
         modelMapper.map(uploadHistory,savedUploadHistory);
        return iUploadHistoryRepository.save(savedUploadHistory);
    }

    @Override
    public void deleteById(String id) {
        iUploadHistoryRepository.deleteById(id);
    }

    @Override
    public List<UploadHistory> findAll() {
        return List.of();
    }
}
