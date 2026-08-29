package com.deodardreams.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Provides BCrypt password hashing so passwords can be securely stored
     * and verified against the hash stored in the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Connects our database-backed UserDetailsService with BCrypt password verification.
     * During login, Spring Security uses CustomUserDetailsService to find the admin user
     * and PasswordEncoder to verify the submitted password against the stored BCrypt hash.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        // Creates the authentication provider and tells it to use our
        // CustomUserDetailsService to load the admin user's email, password hash, and role.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        // Tells the authentication provider to use BCrypt to verify the
        // password entered during login against the stored password hash.
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {

        /**
         * Obtains Spring Security's AuthenticationManager, which coordinates
         * the authentication process using the configured AuthenticationProvider.
         */
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        /**
         * Disables CSRF protection because this application currently exposes
         * stateless REST APIs that are being accessed through clients such as
         * Postman rather than browser-based forms.
         */
        http.csrf(csrf -> csrf.disable());

        /**
         * Enables HTTP Basic authentication.
         *
         * Postman sends the username and password using the Authorization header.
         * Spring Security uses these credentials to authenticate the admin user.
         */
        http.httpBasic(Customizer.withDefaults());

        /**
         * Defines which API endpoints are publicly accessible and which require
         * authentication or specific roles.
         */
        http.authorizeHttpRequests(auth -> auth

                // Customer-facing APIs — no admin authentication required.
                .requestMatchers(
                        "/api/bookings/",
                        "/api/enquiries/**"
                ).permitAll()

                // Only Super Admin can create, update, activate or deactivate admins.
                .requestMatchers("/api/admin-users/**")
                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                // PhysicalUnit and RoomProduct APIs are restricted to Admin and Super Admin.
                .requestMatchers(
                        "/api/physical-units/**",
                        "/api/room-products/**"
                ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Any other API currently requires authentication.
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
        );

        return http.build();
    }

}
