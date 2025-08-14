package com.ecommerce.stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.stock.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    Optional<Product> findById(Long id);
    List<Product> findByCategory(String category);
    List<Product> findAll();
    Product save(Product product);
    void delete(Product product);
    void deleteById(Long id);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
