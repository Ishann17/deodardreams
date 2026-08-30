package com.deodardreams.dto.request;

/**
 * Request used to create an admin user.
 * The plain-text password is accepted only at the API boundary and will be
 * converted into a BCrypt hash before being stored in the database.
 */

import com.deodardreams.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminUserRequestDto {

    @NotNull
    private String name;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String phoneNumber;

    @NotNull
    private String password;

    @NotNull
    private UserRole role;

    // New admin users are active by default.
    private boolean isActive = true;
}