package com.example.crm_system_backend.handler;

import java.util.List;

public interface IHandler<T> {
    T save(T entity);
    List<T> getAll();
    T edit(Long Id,T entity);
    void delete(Long leadId);
    void bulkUpload();
}
