package com.deodardreams.service.admin;

import com.deodardreams.dto.request.CreateAdminUserRequestDto;
import com.deodardreams.dto.request.UpdateAdminUserRequestDto;
import com.deodardreams.dto.response.AdminUserResponseDto;
import com.deodardreams.exception.ResourceNotFoundException;
import com.deodardreams.mapper.AdminUserMapper;
import com.deodardreams.model.AdminUser;
import com.deodardreams.repository.AdminUserRepository;
import com.deodardreams.security.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService{

    private final AdminUserRepository adminUserRepository;
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;


    public AdminUserServiceImpl(AdminUserRepository adminUserRepository, AdminUserMapper adminUserMapper, SecurityConfig securityConfig, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public AdminUserResponseDto createAdminUser(CreateAdminUserRequestDto requestDto) {

        log.info("Creating new admin user with role={}", requestDto.getRole());

        // Maps the request data into an AdminUser entity while intentionally leaving passwordHash unmapped.
        AdminUser adminUserEntity = adminUserMapper.toAdminUserEntity(requestDto);

        // Converts the plain-text password into a BCrypt hash before storing it in the database.
        String passwordHash = passwordEncoder.encode(requestDto.getPassword());
        adminUserEntity.setPasswordHash(passwordHash);

        AdminUser savedAdmin = adminUserRepository.save(adminUserEntity);

        log.info(
                "Admin user created successfully with id={}, role={}",
                savedAdmin.getId(),
                savedAdmin.getRole()
        );

        return adminUserMapper.toAdminUserResponse(savedAdmin);
    }

    @Override
    public List<AdminUserResponseDto> getAllAdminUsers() {

        List<AdminUser> adminUsers = adminUserRepository.findAll();
        return adminUsers.stream().map(adminUserMapper::toAdminUserResponse).toList();
    }

    @Override
    public AdminUserResponseDto updateAdminUser(Long id, UpdateAdminUserRequestDto requestDto) {

        log.info("Updating admin user with id={}", id);

        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Admin with id" + id + " is not available"));

        adminUserMapper.updateAdminUserEntity(requestDto, adminUser);

        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            adminUser.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        }

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("Admin user updated successfully with id={}", id);

        return adminUserMapper.toAdminUserResponse(updatedAdmin);
    }

    @Override
    public void deactivateAdminUser(Long id) {
        log.info("Deactivating admin user with id={}", id);

        // Find the existing admin user. We keep the database record because
        // existing bookings/audit records may still refer to this admin.
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin with id " + id + " is not available"
                        )
                );

        // If the admin is already inactive, there is nothing left to do.
        if (!adminUser.getIsActive()) {
            log.info("Admin user with id={} is already inactive", id);
            return;
        }

        // Soft-deactivate the admin instead of deleting the database record.
        adminUser.setIsActive(false);

        // Persist the inactive status in the database.
        adminUserRepository.save(adminUser);

        log.info("Admin user with id={} deactivated successfully", id);
    }

    @Override
    public void activateAdminUser(Long id) {
        log.info("Activate admin user request received: id={}", id);

        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin with id " + id + " is not available"
                        )
                );

        // If the admin is already active, no database update is required.
        if (adminUser.getIsActive()) {
            return;
        }

        // Reactivate the existing admin without creating a new database record.
        adminUser.setIsActive(true);

        adminUserRepository.save(adminUser);
    }
}
