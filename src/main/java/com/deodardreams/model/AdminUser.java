package com.deodardreams.model;

import com.deodardreams.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
@ToString(exclude = "passwordHash") // never let the password hash leak into logs via toString()
public class AdminUser extends BaseEntity {

    @Column(nullable = false)
    private String name;

    // Login identifier — unique, same reasoning as Guest.phoneNumber being the guest identity key.
    @Column(unique = true, nullable = false)
    @Email
    private String email;

    private String phoneNumber;

    // NEVER a plain password. This stores a one-way BCrypt hash — hashing/verification logic
    // comes later with Spring Security. Even we as developers never know the real password.
    private String passwordHash;

    // Governs what this user can do — SUPER_ADMIN / ADMIN / EMPLOYEE. Guest is not a value here;
    // guests have no admin_users row at all.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // Controls whether this admin is currently allowed to access the application.
    @Column(nullable = false)
    private Boolean isActive = true;
}