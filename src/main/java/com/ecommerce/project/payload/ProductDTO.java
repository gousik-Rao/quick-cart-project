package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    @NotBlank
    @Size(min = 3, message = "Product name must contain at least 3 characters.")
    private String productName;
    private String image;
    @NotBlank
    @Size(min = 6, message = "Product description must contain at least 6 characters.")
    private String description;
    @Positive
    private Integer quantity;
    @Positive
    private double price;
    @PositiveOrZero
    private double discount;
    private double specialPrice;
    private long soldCount;
    private boolean isAvailable;
}
