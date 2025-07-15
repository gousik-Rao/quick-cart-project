package com.ecommerce.project.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.payload.ProductRecommendationRequest;
@Service
public class AiServiceImpl implements AiService {
    private final ChatModel chatModel;

    public AiServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getProductsRecommendations(ProductRecommendationRequest request) {

        if(request.getAge() > 120 || request.getAge() < 0){
            throw new APIException("Please provide valid age.");
        }
        if(request.getGender().getName().equals("MALE") || request.getGender().getName().equals("FEMALE")){
            throw new APIException("Please select valid gender.");
        }
        if(request.getUserGoals().isEmpty()){
            throw new APIException("Goals list must not be empty.");
        }
        if(request.getUserGoals().length() > 300){
            throw new APIException("Goals list must not exceed 300 characters.");
        }

        var structuredRequest = String.format("""
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

        Prompt prompt = new Prompt(structuredRequest,
                OpenAiChatOptions.builder()
                        .withModel("gpt-4o")
                        .withTemperature(0.1F)
                        .withMaxTokens(380)
                        .build());
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }
}
