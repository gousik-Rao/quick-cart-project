package com.ecommerce.project.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.payload.ProductRecommendationRequest;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.AiService;

@RestController
@RequestMapping("/api/recommendations")
public class ProductRecommendationController {

    private final AiService aiService;

    public ProductRecommendationController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<?> getRecommendations(@RequestBody ProductRecommendationRequest request,                
    											@RequestParam(defaultValue = "0")int pageSize, 
    											@RequestParam(defaultValue = "5")int pageNumber) {

    	ProductResponse aiOutput= aiService.getProductsRecommendations(request, pageSize, pageNumber);
    	
    	Map<String, Object> response = Map.of("status", "success", "recommendations", aiOutput);
    	
        return ResponseEntity.ok(response);
    }
}
