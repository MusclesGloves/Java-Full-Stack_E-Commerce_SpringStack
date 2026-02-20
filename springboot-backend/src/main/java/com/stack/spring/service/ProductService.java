package com.stack.spring.service;

import com.stack.spring.dto.ProductRequest;
import com.stack.spring.model.Product;
import com.stack.spring.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product getProductById(int id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + id
                ));
    }

    /**
     * Create a new product. Image is REQUIRED.
     */
    @Transactional
    public Product createProduct(ProductRequest req, MultipartFile imageFile) {
        Product product = new Product();
        applyRequestToEntity(req, product);
        applyImageToEntity(imageFile, product, true); // required
        return productRepo.save(product);
    }

    /**
     * Update existing product. Image is OPTIONAL.
     */
    @Transactional
    public Product updateProduct(int id, ProductRequest req, MultipartFile imageFile) {
        Product existing = getProductById(id); // throws 404 if not found
        applyRequestToEntity(req, existing);
        applyImageToEntity(imageFile, existing, false); // optional
        return productRepo.save(existing);
    }

    @Transactional
    public void deleteProduct(int id) {
        // Ensure 404 if it doesn't exist
        getProductById(id);
        productRepo.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyword is required");
        }
        return productRepo.searchProducts(keyword.trim());
    }

    // ----------------- Helpers -----------------

    private void applyRequestToEntity(ProductRequest req, Product product) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product payload is required");
        }

        // DTO -> Entity mapping
        product.setName(req.getName());
        product.setBrand(req.getBrand());
        product.setDescription(req.getDescription());
        product.setCategory(req.getCategory());
        product.setPrice(req.getPrice());
        product.setStockQuantity(req.getStockQuantity());
        product.setReleaseDate(req.getReleaseDate());
        product.setProductAvailable(req.isProductAvailable());
    }

    private void applyImageToEntity(MultipartFile imageFile, Product product, boolean required) {
        if (imageFile == null || imageFile.isEmpty()) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required");
            }
            return; // optional image on update
        }

        // Basic validation (optional but recommended)
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        try {
            product.setImageName(imageFile.getOriginalFilename());
            product.setImageType(contentType);
            product.setImageData(imageFile.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read image bytes");
        }
    }
}
