package com.ecommerce.project.payload;

import com.ecommerce.project.model.Gender;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProductRecommendationRequest {
    @NotNull
    private Gender gender;
    @Min(1)
    @Max(120)
    @NotNull
    private Integer age;
    @NotNull
    @Max(300)
    private String userGoals;
}

