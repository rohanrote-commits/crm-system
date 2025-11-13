package com.example.crm_system_backend.service;

import com.example.crm_system_backend.entity.UploadHistory;

import java.util.List;

public interface IUploadHistoryService {

    UploadHistory save(UploadHistory uploadHistory);
    UploadHistory findById(String id);
    UploadHistory update(UploadHistory uploadHistory);
    void deleteById(String id);
    List<UploadHistory> findAll();

}
