package com.stack.spring.config;

import com.stack.spring.model.Product;
import com.stack.spring.repo.ProductRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepo repo) {
        return args -> {
            // avoid duplicates by checking existing names
            Set<String> existing = new HashSet<>();
            repo.findAll().forEach(p -> existing.add(p.getName().toLowerCase(Locale.ROOT)));

            addIfMissing(repo, existing,
                    "Apple iPhone 15 Pro", "Apple", "Flagship smartphone, 128GB, 6.1\"",
                    new BigDecimal("999"), "Mobile", 30, "2024-09-20",
                    "iphone15.jpg", "image/jpeg");

            addIfMissing(repo, existing,
                    "Sony WH-1000XM5", "Sony", "Noise-cancelling wireless headphones",
                    new BigDecimal("349"), "Headphone", 50, "2023-08-01",
                    "sonywh.jpg", "image/jpeg");

            addIfMissing(repo, existing,
                    "Dell XPS 13", "Dell", "Ultrabook laptop, 16GB RAM, 512GB SSD",
                    new BigDecimal("1299"), "Laptop", 12, "2024-01-10",
                    "xps13.jpg", "image/jpeg");

            addIfMissing(repo, existing,
                    "Logitech MX Master 3", "Logitech", "Wireless ergonomic mouse",
                    new BigDecimal("99"), "Electronics", 100, "2022-11-05",
                    "mxmaster3.jpg", "image/jpeg");

            addIfMissing(repo, existing,
                    "Samsung 4K Monitor", "Samsung", "27\" UHD HDR display",
                    new BigDecimal("399"), "Electronics", 25, "2023-05-15",
                    "samsung4k.jpg", "image/jpeg");
        };
    }

    private static void addIfMissing(
            ProductRepo repo, Set<String> existing,
            String name, String brand, String desc,
            BigDecimal price, String category, int stock, String date,
            String imageFileName, String imageType) throws Exception {

        if (existing.contains(name.toLowerCase(Locale.ROOT))) return;

        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setDescription(desc);
        p.setPrice(price);
        p.setCategory(category);
        p.setStockQuantity(stock);
        p.setProductAvailable(stock > 0);
        p.setReleaseDate(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE));
        p.setImageName(imageFileName);
        p.setImageType(imageType);

        byte[] bytes = new ClassPathResource("seed-images/" + imageFileName).getContentAsByteArray();
        p.setImageData(bytes);

        repo.save(p);
    }
}
