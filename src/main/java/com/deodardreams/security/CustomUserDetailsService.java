package com.deodardreams.security;

/**
 * Loads an admin user from the database using the email supplied during login.
 * Spring Security uses this service to retrieve the user's credentials and role.
 *
 * Login email
 *     ↓
 * CustomUserDetailsService
 *     ↓
 * AdminUserRepository.findByEmail()
 *     ↓
 * AdminUser
 *     ↓
 * Spring Security UserDetails
 */

import com.deodardreams.model.AdminUser;
import com.deodardreams.repository.AdminUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public CustomUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        //Find Admin user by their Email
        AdminUser adminUser = adminUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException( "Admin user not found with email: " + email));

        //converting your database user into the format Spring Security understands
        return User.withUsername(adminUser.getEmail())
                .password(adminUser.getPasswordHash())
                .roles(adminUser.getRole().name())
                .build();
    }
}
