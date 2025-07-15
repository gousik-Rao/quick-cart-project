package com.ecommerce.project.service;

import org.springframework.stereotype.Service;

import com.ecommerce.project.payload.ProductRecommendationRequest;

@Service
public interface AiService  {
    String getProductsRecommendations(ProductRecommendationRequest request);
}
