package com.ecommerce.stock.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.stock.model.Order;
import com.ecommerce.stock.model.OrderResult;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.repository.ProductRepository;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {        
        logger.info("Fetching all products");
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Long id) {
        logger.info("Fetching product with id: {}", id);
        return productRepository.findById(id);
        
    }

    public Product createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        logger.info("Updating product with id: {}", id);
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setStockQuantity(productDetails.getStockQuantity());
                    product.setCategory(productDetails.getCategory());
                    product.setStockQuantity(productDetails.getStockQuantity());
                    logger.info("Product with id {} updated successfully.", id);                    
                    return productRepository.save(product);                    
                })
                .orElseGet(() -> {
                    logger.warn("Product with id {} not found for update.", id);
                    return null;
                });
    }
    public boolean deleteProduct(Long id) {
        logger.info("Deleting product with id: {}", id);
        productRepository.findById(id)
                .ifPresent( product -> {
                    logger.info("Product with id {} found for deletion.", id);
                    productRepository.delete(product);
                    logger.info("Product with id {} deleted successfully.", id);                    
                });
        logger.warn("Product with id {} not found for deletion.", id);
        return false;
    }    
    public List<Product> findByCategory(String category) {
        logger.info("Fetching products by category: {}", category);
        return productRepository.findByCategory(category);
    }
    public List<Product> findByPriceBetween(Double minPrice, Double maxPrice) {
        logger.info("Fetching products with price between {} and {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public boolean buyProduct(Long id, int quantity) {
        logger.info("Buying product with id: {} and quantity: {}", id, quantity);
        return productRepository.findById(id)
                .map(product -> {
                    if (product.getStockQuantity() >= quantity) {
                        product.decreaseStock(quantity);
                        productRepository.save(product);
                        logger.info("Product with id {} bought successfully.", id);
                        return true;
                    } else {
                        logger.warn("Insufficient stock for product with id {}.", id);
                        return false;
                    }
                })
                .orElseGet(() -> {
                    logger.warn("Product with id {} not found for buying.", id);
                    return false;
                });
    }

    public String buyProducts(Order order) {
        logger.info("Processing order for products with total price: {}", order.getTotalPrice());
        StringBuilder response = new StringBuilder();
        List<OrderResult> results = new ArrayList<OrderResult>();
        for(Long key : order.getProductsQuantity().keySet()) {
            Product product = productRepository.findById(key).orElse(null);
            OrderResult result = new OrderResult();
            if (product != null) {                
                int quantity = order.getProductsQuantity().get(key);
                if (product.getStockQuantity() >= quantity) {
                    product.decreaseStock(quantity);
                    productRepository.save(product);                    
                    result.setProduct(product);
                    result.setSuccess(true);
                    result.setResponse("Order successful for product: " + product.getName());
                    results.add(result);
                } else {                    
                    result.setProduct(product);
                    result.setSuccess(false);
                    result.setResponse("Insufficient stock for product: " + product.getName());
                    results.add(result);
                }
            } else {                
                result.setSuccess(false);
                result.setResponse("Product not found with id: " + key);
                results.add(result);
                result.setProduct(new Product());
                result.getProduct().setNotFound();
            }
            response.append("Product ID: ").append(result.getProduct().getId())
                   .append(", Name: ").append(result.getProduct().getName())
                   .append(", Quantity: ").append(order.getProductsQuantity().get(key))
                   .append(", Status:").append(result.isSuccess() ? "Success" : "Failed")
                   .append(", Reason: ").append(result.getResponse())
                   .append("\n");
        }
        logger.info("Order processed with results: {}", results);
        return response.toString();
    }


    public boolean increaseStock(Long id, int quantity) {
        logger.info("Increasing stock for product with id: {} by quantity: {}", id, quantity);
        return productRepository.findById(id)
                .map(product -> {
                    product.increaseStock(quantity);
                    productRepository.save(product);
                    logger.info("Stock for product with id {} increased successfully.", id);
                    return true;
                })
                .orElseGet(() -> {
                    logger.warn("Product with id {} not found for stock increase.", id);
                    return false;
                });
    }
}
