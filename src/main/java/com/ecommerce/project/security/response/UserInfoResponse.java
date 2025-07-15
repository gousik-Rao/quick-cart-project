package com.ecommerce.project.security.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String username;
    private String phoneNumber;
    private String email;
    private List<String> roles;
    private Double balance;
    private Double totalEarnings;

}