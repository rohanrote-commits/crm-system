package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.Product;
import com.example.crm_system_backend.exception.ProductException;
import com.example.crm_system_backend.repository.ProductRepo;
import com.example.crm_system_backend.service.IProductService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@Service
@AllArgsConstructor
public class ProductService implements IProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepo productRepo;

    @Override
    public Set<Product> getProducts() {
        log.info("Enter: ProductService.getProducts()");
       return new HashSet<>(productRepo.findAll());
    }

    @Override
    public Product getProductById(Long id) {
        log.info("Enter: ProductService.getProductById()");
      Product product =   productRepo.getProductById(id).orElseThrow(
                ()->{
                    log.error("Exception: ProductService.getProductById()-> Product not found with id: {}", id);
                   return new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
                }
        );
      log.info("Exit: ProductService.getProductById()");
        return product ;
    }

    @Override
    public Product getProductByName(String name) {

        log.info("Enter: ProductService.getProductByName()");
        Product product = productRepo.getProductByProductName(name.trim()).orElseThrow(
                ()->{
                    log.error("Exception: ProductService.getProductByName()-> Product not found with name: {}",name);
                    return new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
                }
        );
        log.info("Exit: ProductService.getProductByName()");
        return product;
    }


}
