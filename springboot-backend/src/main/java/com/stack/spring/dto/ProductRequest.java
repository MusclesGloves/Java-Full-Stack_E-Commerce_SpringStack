package com.stack.spring.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank private String name;
    @NotBlank private String brand;
    @NotBlank private String description;
    @NotBlank private String category;

    @NotNull @Positive private BigDecimal price;
    @NotNull @Min(0) private Integer stockQuantity;

    @NotNull private LocalDate releaseDate;
    private boolean productAvailable;

    // getters and setters
}
