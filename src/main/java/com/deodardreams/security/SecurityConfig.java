package com.deodardreams.security;

/**
 * Central Spring Security configuration for the whole application. This is
 * where authentication (WHO are you) and authorization (WHAT are you allowed
 * to do) get wired together and applied to every incoming HTTP request.
 *
 * ANNOTATIONS USED IN THIS FILE, EXPLAINED:
 *
 * @Configuration
 *   Tells Spring "this class defines beans" — objects Spring should create
 *   once, at startup, and manage for the rest of the app's life. Every
 *   @Bean method below only runs because this class is marked this way.
 *
 * @EnableMethodSecurity
 *   Turns on support for @PreAuthorize on individual controller methods
 *   (used in AdminUserController). Without this annotation, @PreAuthorize
 *   annotations would be silently ignored — this is the switch that makes
 *   Spring actually check them.
 *
 * @Bean (used four times below)
 *   Marks a method whose RETURN VALUE Spring should register as a managed
 *   object, so it can be automatically injected wherever it's needed
 *   elsewhere in the app (e.g. AdminUserServiceImpl asking for a
 *   PasswordEncoder in its constructor).
 *
 * THE FOUR BEANS, IN THE ORDER THEY WORK TOGETHER:
 *
 * 1. passwordEncoder() — provides BCrypt, the one-way hashing algorithm used
 *    to turn a real password into an unreadable hash before it's ever saved,
 *    and to check a login attempt against that hash later.
 *
 * 2. authenticationProvider() — the piece that actually PERFORMS a login
 *    check. It uses CustomUserDetailsService to fetch the admin user by
 *    email, then uses the passwordEncoder above to verify the submitted
 *    password matches the stored hash.
 *
 * 3. authenticationManager() — a small coordinator Spring Security uses
 *    internally to route a login attempt to the right authenticationProvider.
 *    Exists here mainly because Spring Security's setup requires exposing it
 *    explicitly as a bean.
 *
 * 4. securityFilterChain() — the actual rulebook applied to every request
 *    BEFORE it reaches any controller. Three things happen inside it:
 *      - csrf().disable() — turns off a browser-form protection mechanism
 *        that doesn't apply here, since this API is called by REST clients
 *        (Postman, a future frontend), not browser-submitted HTML forms.
 *      - httpBasic() — turns on HTTP Basic Auth, meaning a caller proves who
 *        they are by sending an email+password pair in the request's
 *        Authorization header, on every request (no separate login step
 *        or session/token needed).
 *      - authorizeHttpRequests() — the actual per-URL rulebook: which paths
 *        are open to everyone (bookings, enquiries), and which require the
 *        caller to hold specific roles (admin-users, physical-units,
 *        room-products). Rules are checked TOP TO BOTTOM, and the first
 *        matching rule wins — order matters here.
 *
 * IMPORTANT: this URL-level rulebook is only the FIRST of two checkpoints a
 * request must pass. Some controller methods (see AdminUserController) add a
 * SECOND, more specific @PreAuthorize check on top of this one. A request
 * must satisfy BOTH to succeed — this file's rules are the broad outer gate,
 * @PreAuthorize is the specific inner gate for one particular action.
 */

import com.deodardreams.security.jwt.JwtAccessDeniedHandler;
import com.deodardreams.security.jwt.JwtAuthenticationEntryPoint;
import com.deodardreams.security.jwt.JwtAuthenticationFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

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
                .requestMatchers("/api/bookings/**", "/api/enquiries/**", "/api/auth/**").permitAll()

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
        /**
         * Inserts our custom JwtAuthenticationFilter into Spring Security's filter
         * chain, positioned to run BEFORE UsernamePasswordAuthenticationFilter (the
         * filter that handles Basic Auth). This means: for every incoming request,
         * our filter gets first chance to check for a valid bearer token and, if
         * found, mark the request as authenticated before Basic Auth is even
         * considered. If no valid token is present, the request simply falls
         * through to the existing Basic Auth path unchanged — both mechanisms
         * currently work side by side.
         */
        http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint));
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
        );
        return http.build();
    }

}
