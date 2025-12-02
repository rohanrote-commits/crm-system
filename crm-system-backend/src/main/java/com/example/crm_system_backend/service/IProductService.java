package com.example.crm_system_backend.service;

import com.example.crm_system_backend.entity.Product;

import java.util.Set;

public interface IProductService {
    Set<Product> getProducts();
     Product getProductById(Long id);
    Product getProductByName(String name);
}
