package com.deodardreams.repository;

import com.deodardreams.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    /**
     * Finds an admin user by the unique email used as the login identifier.
     */
    Optional<AdminUser> findByEmail(String email);
}
