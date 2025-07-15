package com.ecommerce.project.security.services;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.UserDTO;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.util.AuthUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

@Service 
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;
//	private final JwtUtils jwtUtils;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private final RoleRepository roleRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, 
    		AuthUtil authUtil, 
    		PasswordEncoder passwordEncoder, 
//    		JwtUtils jwtUtils, 
    		RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.authUtil = authUtil;
        this.passwordEncoder = passwordEncoder;
//        this.jwtUtils = jwtUtils;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }


    @Transactional
    public UserInfoResponse updateUser(UserDTO userDTO) {
        System.out.println(userDTO);
        Long currUserId = authUtil.loggedInUserId();

        User user = userRepository.findById(currUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", currUserId));

        String normalizedPhoneNumber = userDTO.getPhoneNumber().replaceAll("\\s+", "");
        validateCredentials(userDTO.getUsername(), userDTO.getEmail(), normalizedPhoneNumber, user);

        user.setUserName(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        String phoneNumber = validatePhoneNumber(userDTO.getPhoneNumber());
        user.setPhoneNumber(phoneNumber);
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(user);

        UserDetails updatedUserDetails = loadUserByUsername(user.getUserName());
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                updatedUserDetails.getPassword(),
                updatedUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        List<String> roles = user.getRoles().stream().map((role) -> role.getRoleName().name()).toList();


        return new UserInfoResponse(currUserId,user.getUserName(),user.getPhoneNumber(),user.getEmail(),roles, user.getBalance(), user.getTotalEarnings());
    }

    @Transactional
    public UserInfoResponse becomeSeller() {
        User currUser = authUtil.loggedInUser();

        if(currUser.getRoles().stream().noneMatch(role->role.getRoleName().equals(AppRole.ROLE_SELLER)|| role.getRoleName().equals(AppRole.ROLE_ADMIN))){
            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                    .orElseGet(() -> {
                        Role newSellerRole = new Role(AppRole.ROLE_SELLER);
                        return roleRepository.save(newSellerRole);
                    });
            currUser.getRoles().add(sellerRole);
            userRepository.save(currUser);

            UserDetails updatedUserDetails = loadUserByUsername(currUser.getUserName());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails,
                    updatedUserDetails.getPassword(),
                    updatedUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
        List<String> roles = currUser.getRoles().stream().map((role) -> role.getRoleName().name()).toList();
        return new UserInfoResponse(currUser.getUserId(), currUser.getUserName(),currUser.getPhoneNumber(),currUser.getEmail(),roles, currUser.getBalance(), currUser.getTotalEarnings());


    }

    private void validateCredentials(String userName, String email,String phoneNumber, User user) {
        if(!user.getUserName().equals(userName) && userRepository.existsByUserName(userName)){
            throw new APIException("Username is already taken.");
        }
        if(!user.getEmail().equals(email)&&userRepository.existsByEmail(email)){
            throw new APIException("Email is already in use.");
        }
        if(!user.getPhoneNumber().equals(phoneNumber) && userRepository.existsByPhoneNumber(phoneNumber)){
            throw new APIException("Phone number is already in use.");
        }

    }

    public String validatePhoneNumber(String phoneNumber) {
        try{
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, null);
            boolean valid = phoneUtil.isValidNumber(numberProto);
            if (!valid) {
                throw new APIException("Invalid phone number format.");
            }
            return phoneNumber;
        } catch (NumberParseException e) {
            throw new APIException("Invalid phone number.");
        }
    }


}