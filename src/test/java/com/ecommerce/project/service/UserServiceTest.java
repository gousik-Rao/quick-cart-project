package com.ecommerce.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	
	@Mock
	private UserRepository userRepo;
	
	@InjectMocks
	private UserDetailsServiceImpl userService;
	
	@Test
	public void testLoadUserByUsername_UserExists() {
		User mockUser = new User();
		mockUser.setUserName("testuser");
		mockUser.setPassword("password123");
		
		when(userRepo.findByUserName("testuser"))
				.thenReturn(Optional.of(mockUser));
		
		UserDetails userDetails = userService.loadUserByUsername("testuser");
		
		// Assert 
		assertNotNull(userDetails);
		assertEquals("testuser", userDetails.getUsername());
	}
	
	@Test
	public void testLoadUserByUsername_UserNotFound() {
		when(userRepo.findByUserName("non-existent"))
			.thenReturn(Optional.empty());
	
		// Assert
		assertThrows(UsernameNotFoundException.class, () -> {
			userService.loadUserByUsername("non-existent");
		});
	}
}
