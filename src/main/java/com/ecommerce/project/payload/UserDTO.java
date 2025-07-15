package com.ecommerce.project.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class UserDTO {
    @NotBlank
    @Size(min = 3, max=20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @Size(min = 8)
    private String phoneNumber;

    @Size(min = 6, max = 40)
    private String password;
}
