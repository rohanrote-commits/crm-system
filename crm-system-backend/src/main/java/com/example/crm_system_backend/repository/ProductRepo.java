package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    Optional<Product> getProductById(Long id);

    Optional<Product> getProductByProductName(String name);

    Product findProductByProductName(String name);

    boolean existsByProductName(String moduleName);
}
