package com.deodardreams.dto.request;

/**
 * Credentials submitted to log in as an admin user. Nothing more than
 * identify + verify — no other admin fields belong here.
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AdminLoginRequestDto {

    @NotBlank
    @Email
    String email;

    @NotBlank
    String password;
}
