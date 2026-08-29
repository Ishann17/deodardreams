/*
package com.deodardreams.util;

import com.deodardreams.enums.UserRole;
import com.deodardreams.model.AdminUser;
import com.deodardreams.repository.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // Create the first Super Admin only when the AdminUser table is empty.
        if (adminUserRepository.count() == 0){
            AdminUser superAdmin = new AdminUser();
            superAdmin.setName();
            superAdmin.setEmail();
            superAdmin.setPhoneNumber();

            // Never store the plain-text password in the database.
            superAdmin.setPasswordHash(
                    passwordEncoder.encode()
            );

            superAdmin.setRole();
            superAdmin.setIsActive();

            adminUserRepository.save(superAdmin);
        }
    }
}
*/
