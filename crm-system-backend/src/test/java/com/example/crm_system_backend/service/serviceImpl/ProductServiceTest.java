package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.entity.Product;
import com.example.crm_system_backend.exception.ProductException;
import com.example.crm_system_backend.repository.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private ProductService productService;
    private ProductRepo productRepo;

    @BeforeEach
    void setUp() {
        productRepo = mock(ProductRepo.class);
        productService = new ProductService(productRepo);
    }

    @Test
    void getProducts_Success() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setProductName("Product A");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setProductName("Product B");

        when(productRepo.findAll()).thenReturn(List.of(product1, product2));

        // Act
        Set<Product> result = productService.getProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(product1));
        assertTrue(result.contains(product2));
        verify(productRepo, times(1)).findAll();
    }

    @Test
    void getProducts_Empty() {
        // Arrange
        when(productRepo.findAll()).thenReturn(List.of());

        // Act
        Set<Product> result = productService.getProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepo, times(1)).findAll();
    }

    @Test
    void getProductById_Success() {

        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setProductName("Test Product");

        when(productRepo.getProductById(productId)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getProductName());
        verify(productRepo, times(1)).getProductById(productId);
    }

    @Test
    void getProductById_NotFound() {
        // Arrange
        Long productId = 1L;
        when(productRepo.getProductById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductException exception = assertThrows(ProductException.class,
            () -> productService.getProductById(productId));

        assertEquals("PRODUCT NOT EXIST",exception.getMessage());
        verify(productRepo, times(1)).getProductById(productId);
    }

    @Test
    void getProductByName_Success() {

        String productName = "Product A";
        Product product = new Product();
        product.setId(1L);
        product.setProductName(productName);

        when(productRepo.getProductByProductName(productName)).thenReturn(Optional.of(product));

        Product result = productService.getProductByName(productName);

        assertNotNull(result);
        assertEquals(productName, result.getProductName());
        verify(productRepo, times(1)).getProductByProductName(productName);
    }

    @Test
    void getProductByName_NotFound() {

        String productName = "Nonexistent Product";
        when(productRepo.getProductByProductName(productName)).thenReturn(Optional.empty());

        ProductException exception = assertThrows(ProductException.class,
            () -> productService.getProductByName(productName));

        assertEquals("PRODUCT NOT EXIST", exception.getMessage());
        verify(productRepo, times(1)).getProductByProductName(productName);
    }

}