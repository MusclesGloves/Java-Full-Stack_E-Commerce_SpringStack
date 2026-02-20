package com.stack.spring.controller;

import com.stack.spring.dto.ProductRequest;
import com.stack.spring.model.Product;
import com.stack.spring.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET all products
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET product by ID
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // GET product image (returns correct content-type)
    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable int productId) {
        Product product = productService.getProductById(productId);

        byte[] data = product.getImageData();
        if (data == null || data.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (product.getImageType() != null && !product.getImageType().isBlank()) {
                mediaType = MediaType.parseMediaType(product.getImageType());
            }
        } catch (Exception ignored) { }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(product.getImageData());
    }

    // CREATE product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> addProduct(
            @Valid @RequestPart("product") ProductRequest req,
            @RequestPart("imageFile") MultipartFile imageFile
    ) {
        Product saved = productService.createProduct(req, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // UPDATE product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateProduct(
            @PathVariable int id,
            @Valid @RequestPart("product") ProductRequest req,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        Product updated = productService.updateProduct(id, req, imageFile);
        return ResponseEntity.ok(updated);
    }

    // DELETE product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Deleted");
    }

    // SEARCH products
    @GetMapping("/product/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }
}
