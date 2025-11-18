package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Integer> {

    Optional<Product> getProductById(Long id);

    Optional<Product> getProductByModuleName(String name);

    boolean existsByModuleName(String moduleName);
}
