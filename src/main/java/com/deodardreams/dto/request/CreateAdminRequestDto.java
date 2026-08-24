package com.deodardreams.dto.request;

/**
 * Data submitted to register a new admin/employee account. Note: this
 * carries a plain-text password from the client — the service layer hashes
 * it with BCrypt before it ever reaches AdminUser.passwordHash. This DTO
 * should never contain a pre-hashed value.
 */

import com.deodardreams.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminRequestDto {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain letters only")
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @NotBlank
    private String password;

    @NotNull
    private UserRole role;
}
