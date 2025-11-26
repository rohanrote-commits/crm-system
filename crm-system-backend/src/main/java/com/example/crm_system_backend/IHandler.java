package com.example.crm_system_backend;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IHandler<T> {
    T save(T entity);

    List<T> getAll();

    T edit(Long Id, T entity);

    void delete(Long leadId);

    void bulkUpload(MultipartFile file, Long id);
}
