package com.ecommerce.project.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductRecommendationRequest;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.specification.ProductSpecification;

import reactor.core.publisher.Mono;

@Service @SuppressWarnings("unchecked")
public class AiServiceImpl implements AiService {
	
	private final ProductRepository productRepo;
    private final WebClient webClient;
    
    @Value("${spring.google.ai.studio.api.key}")
    private String apiKey;
    
    @Value("${spring.google.ai.studio.model.name}")
    private String model;

    public AiServiceImpl(WebClient.Builder builder, ProductRepository productRepo) {
    	this.productRepo = productRepo;
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();            
    }

    @Override
    public ProductResponse getProductsRecommendations(ProductRecommendationRequest request, int pageSize, int pageNumber) {

    	// 🔐 Basic validation
        if(request.getAge() > 120 || request.getAge() < 0){
            throw new APIException("Please provide valid age.");
        }
        if(!("MALE".equalsIgnoreCase(request.getGender().getName()) ||
        	"FEMALE".equalsIgnoreCase(request.getGender().getName()))){
            throw new APIException("Please select valid gender.");
        }
        if(request.getUserGoals().isEmpty()){
            throw new APIException("Goals list must not be empty.");
        }
        if(request.getUserGoals().length() > 300){
            throw new APIException("Goals list must not exceed 300 characters.");
        }

        // Prompt Generation
        var prompt = String.format("""
                You are a supplement advisor for an online store. Act as a knowledgeable and friendly assistant.
                You only have 300 tokens available to respond.
                                                Based on the user's information, generate a list of recommended supplements.
                                                Include supplement names and a short explanation of why each is recommended.
                
                                                User Information:
                                                - Age: %d
                                                - Gender: %s
                                                - Goals: %s
                
                                                Respond in a helpful and informative tone.
                                                Provide the list as bullet points.
                                                Limit the list to 5-7 items.
                """,request.getAge(), request.getGender(), request.getUserGoals());

       Map<String, Object> body = Map.of(
    		   "contents", List.of(
    				   Map.of("parts", List.of(Map.of("text", prompt)))
    			)
       );
       
       System.out.println("Calling: https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey);

       // 🔌 Gemini API call
       String aiOutput = webClient.post()
    		   .uri(uriBuilder -> uriBuilder
    				   .path("/v1beta/models/" + model + ":generateContent")
    				   .queryParam("key", apiKey)
    				   .build())
    		   .contentType(MediaType.APPLICATION_JSON)
    		   .bodyValue(body)
    		   .retrieve()
    		   .bodyToMono(Map.class)
    		   .map(response -> {
    			   
				List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
    			   if (candidates != null && !candidates.isEmpty()) {
                       Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                       List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                       return (String) parts.get(0).get("text");
                   }
                   return "No recommendation generated";
               })
               .onErrorResume(ex -> Mono.just("AI Error: " + ex.getMessage()))
               .block(); // Optional: you can make this reactive by returning Mono<String>
    		   
       if(aiOutput == null || aiOutput.isBlank())
    	   return new ProductResponse(List.of(), 0, 0, 0L, 0, true);
       
       // 🔍 Extract product names
       List<String> productNames = extractProductNames(aiOutput);
       
       // Search DB by names
       Pageable pageable = PageRequest.of(0, 5);
       Page<Product> matchedProducts = productRepo.findAll(ProductSpecification.productNameLikeAny(productNames), pageable);

       // 🧾 Map to DTOs
       List<ProductDTO> productDTOs = matchedProducts.stream()
               .map(this :: convertToDTO)
               .collect(Collectors.toList());

       // Build ProductResponse
       return new ProductResponse(
               productDTOs,
               0,                                // pageNumber
               productDTOs.size(),               // pageSize
               (long) productDTOs.size(),        // totalElements
               1,                                // totalPages
               true                              // lastPage
       );
   }

   // 🔍 Extract product names from Gemini's response
   private List<String> extractProductNames(String aiOutput) {
       return Arrays.stream(aiOutput.split("\n"))
    		   .filter(line -> line.startsWith("*") || line.startsWith("-"))
    		   .map(line -> line.replaceAll("*\\-\\d", "")
    				   			.split(":")[0]
    				   			.trim())
    		   .filter(name -> !name.isBlank())
    		   .toList();
   }
   
   // Converting the List to DTO of Product
   private ProductDTO convertToDTO(Product product){
	   return new ProductDTO(
               product.getProductId(),
               product.getProductName(),
               product.getImage(),
               product.getDescription(),
               product.getQuantity(),
               product.getPrice(),
               product.getDiscount(),
               product.getSpecialPrice(),
               product.getSoldCount(),
               product.getIsAvailable()
       );
   }
}







