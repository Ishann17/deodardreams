package com.deodardreams.security;

/**
 * Bridges YOUR database's AdminUser table to Spring Security's own login
 * system. Spring Security doesn't know anything about your AdminUser entity
 * — it only understands a generic shape called UserDetails. This class's
 * one job is translating between the two, every time someone tries to log in.
 *
 * THE FULL LOGIN FLOW, STEP BY STEP:
 *
 * 1. A request arrives with an email + password in the Authorization header
 *    (see SecurityConfig's httpBasic() setting).
 * 2. Spring Security's authenticationProvider (see SecurityConfig) calls
 *    THIS class's loadUserByUsername(email) to go find that user.
 * 3. This class asks AdminUserRepository to find the matching AdminUser row
 *    by email — throwing UsernameNotFoundException if no such admin exists,
 *    which Spring Security treats as a failed login.
 * 4. The found AdminUser (your own entity, with your own fields — name,
 *    phoneNumber, passwordHash, role, isActive) gets converted into a
 *    Spring Security UserDetails object — the generic shape Spring Security
 *    actually knows how to work with.
 * 5. Spring Security then separately checks the submitted password against
 *    the returned UserDetails' password hash (using the PasswordEncoder bean
 *    from SecurityConfig) — that comparison does NOT happen in this class.
 *
 * ANNOTATIONS AND METHODS EXPLAINED:
 *
 * @Service
 *   Registers this class as a Spring-managed bean, the same way @Service is
 *   used everywhere else in this project (e.g. BookingServiceImpl) — it lets
 *   Spring create one instance and inject it wherever needed, here into
 *   SecurityConfig's authenticationProvider() bean.
 *
 * implements UserDetailsService
 *   This is a contract Spring Security itself defines — it says "any class
 *   that implements this interface can be used to look up a user during
 *   login." Spring Security calls the one required method below
 *   automatically; you never call it yourself directly.
 *
 * loadUserByUsername(String email)
 *   The single method this interface requires. Despite the parameter being
 *   named "username" (a generic term from the interface's own design), it
 *   receives whatever identifier the app actually logs in with — in this
 *   app, that's email, not a separate username field.
 *
 * User.withUsername(...).password(...).roles(...).disabled(...).build()
 *   A builder provided by Spring Security itself (org.springframework
 *   .security.core.userdetails.User) for constructing a UserDetails object
 *   field by field:
 *     - withUsername(...) — the identifier used to log in (this app's email)
 *     - password(...)     — the STORED HASH to check against, never the raw
 *                            password itself
 *     - roles(...)        — this admin's role(s), used later by
 *                            hasRole(...)/hasAnyRole(...) checks in
 *                            SecurityConfig and @PreAuthorize
 *     - disabled(...)     — if true, Spring Security rejects the login
 *                            entirely before even checking the password;
 *                            here it's tied to isActive, so a deactivated
 *                            admin can never log in again until reactivated
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
                .disabled(!adminUser.getIsActive()) // Disabled Admins will not be able to log in.
                .build();
    }
}
