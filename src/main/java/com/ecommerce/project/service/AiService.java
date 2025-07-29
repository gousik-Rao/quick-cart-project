package com.ecommerce.project.service;

import org.springframework.stereotype.Service;

import com.ecommerce.project.payload.ProductRecommendationRequest;
import com.ecommerce.project.payload.ProductResponse;

@Service
public interface AiService  {
    ProductResponse getProductsRecommendations(
    		ProductRecommendationRequest request,
    		int pagesize,
    		int pageNumber);
}
