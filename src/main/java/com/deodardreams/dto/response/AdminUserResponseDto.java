package com.deodardreams.dto.response;

import com.deodardreams.enums.UserRole;

/**
 * Response returned to administrators after an admin-user operation.
 *
 * Sensitive authentication data such as the password and password hash
 * are intentionally excluded.
 */
public record AdminUserResponseDto(
        Long id,
        String name,
        String email,
        String phoneNumber,
        UserRole role,
        boolean isActive
) {}