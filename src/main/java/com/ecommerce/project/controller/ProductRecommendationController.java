package com.ecommerce.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.payload.ProductRecommendationRequest;
import com.ecommerce.project.service.AiService;

@RestController
@RequestMapping("/api/recommendations")
public class ProductRecommendationController {

    private final AiService aiService;

    public ProductRecommendationController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<String> getRecommendations(@RequestBody ProductRecommendationRequest request) {
    	System.out.println("GOOGLE_APPLICATION_CREDENTIALS: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

    	String aiOutput= aiService.getProductsRecommendations(request);
        return new ResponseEntity<>(aiOutput, HttpStatus.OK);
    }


}
