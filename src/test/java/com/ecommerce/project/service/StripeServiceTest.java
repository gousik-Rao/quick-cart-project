package com.ecommerce.project.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import com.ecommerce.project.payload.StripePaymentDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@SpringBootTest
public class StripeServiceTest {

	@InjectMocks
	private StripeServiceImpl stripeService;
	
	@Test
	void testPaymentIntentCreation() throws StripeException{
		// Prepare
		StripePaymentDTO dto = new StripePaymentDTO();
		dto.setAmount(10000L); // ₹100 (10000 paisa)
		dto.setCurrency("inr");
		
		// Since PaymentIntent.create is static, we cannot mock it directly.
        // Instead we can wrap it in another service or use integration tests instead.

		// Assert
		PaymentIntent intent = stripeService.paymentIntent(dto);
		assertNotNull(intent.getClientSecret());
	}
}
