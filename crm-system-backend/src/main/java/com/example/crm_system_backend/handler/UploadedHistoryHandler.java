package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Component
@AllArgsConstructor
public class UploadedHistoryHandler {

    private  final UploadHistoryService uploadHistoryService;

    private ModelMapper modelMapper;



    public List<UploadHistoryDto> findUploadHistoryByEmail(String email)
    {
        List<UploadHistoryDto> uploadHistoryDtos =  uploadHistoryService.findByUser(email).stream().map(uploadHistory ->
        {
            return modelMapper.map(uploadHistory,UploadHistoryDto.class);
        }).toList();
        return uploadHistoryDtos;
    }

    public UploadHistoryDto findUploadHistoryById(@PathVariable String uploadHistoryId){
     UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
     return modelMapper.map(uploadHistory,UploadHistoryDto.class);
    }
}
