package com.ecommerce.stock.repository;

    import com.bankai.mcpserver.model.Product;
    import com.ecommerce.stock.model.Product;
    
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.Optional;
    import java.util.List;

public interface ProductRepository {
    Optional<Product> findByName(String name);
    Optional<Product> findById(Long id);
    List<Product> findByCategory(String category);
    List<Product> findAll();
    Product save(Product product);
    void delete(Product product);
    void deleteById(Long id);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
