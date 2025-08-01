package com.ecommerce.project.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ecommerce.project.payload.StripePaymentDTO;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = "{USER}")
public class StripeIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objMapper;
	
	@Mock
	private JwtUtils jwtUtils;
	
	@Test
	public void testStripeClientSecretGeneration() throws Exception{
		StripePaymentDTO dto = new StripePaymentDTO();
		dto.setAmount(10000L); // ₹100 (10000 paisa)
		dto.setCurrency("inr");
		
		
		MvcResult result = mockMvc.perform(post("/api/order/stripe-client-secret")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andReturn();
		
		String responseBody = result.getResponse().getContentAsString();
		assertNotNull(responseBody);
	}

}
