package com.telusko.springecom.controller;

import com.telusko.springecom.dto.ProductRequest;
import com.telusko.springecom.model.Product;
import com.telusko.springecom.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET all products
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    // GET product by ID
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Product product = productService.getProductById(id);
        if (product != null && product.getId() > 0) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // GET product image
    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable int productId) {
        Product product = productService.getProductById(productId);
        if (product != null && product.getId() > 0) {
            return new ResponseEntity<>(product.getImageData(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // CREATE product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @Valid @RequestPart("product") ProductRequest req,
            @RequestPart("imageFile") MultipartFile imageFile) {

        try {
            if (imageFile.isEmpty()) {
                return new ResponseEntity<>("Image is required", HttpStatus.BAD_REQUEST);
            }

            // Map DTO -> Entity
            Product product = new Product();
            product.setName(req.getName());
            product.setBrand(req.getBrand());
            product.setDescription(req.getDescription());
            product.setCategory(req.getCategory());
            product.setPrice(req.getPrice());
            product.setStockQuantity(req.getStockQuantity());
            product.setReleaseDate(req.getReleaseDate());
            product.setProductAvailable(req.isProductAvailable());

            Product savedProduct = productService.addOrUpdateProduct(product, imageFile);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @Valid @RequestPart("product") ProductRequest req,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            Product existing = productService.getProductById(id);
            if (existing == null || existing.getId() <= 0) {
                return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
            }

            // Map DTO -> existing entity
            existing.setName(req.getName());
            existing.setBrand(req.getBrand());
            existing.setDescription(req.getDescription());
            existing.setCategory(req.getCategory());
            existing.setPrice(req.getPrice());
            existing.setStockQuantity(req.getStockQuantity());
            existing.setReleaseDate(req.getReleaseDate());
            existing.setProductAvailable(req.isProductAvailable());

            Product updatedProduct = productService.addOrUpdateProduct(existing, imageFile);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            productService.deleteProduct(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
        }
    }

    // SEARCH products
    @GetMapping("/product/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        System.out.println("searching with :" + keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
}
