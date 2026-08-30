package com.deodardreams.controller.admin;

import com.deodardreams.dto.request.CreateAdminUserRequestDto;
import com.deodardreams.dto.request.UpdateAdminUserRequestDto;
import com.deodardreams.dto.response.AdminUserResponseDto;
import com.deodardreams.service.admin.AdminUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin-users")
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Creates a new admin user.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminUserResponseDto> createAdminUser(
            @Valid @RequestBody CreateAdminUserRequestDto requestDto) {

        log.info(
                "Create admin user request received: email={}, role={}",
                requestDto.getEmail(),
                requestDto.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminUserService.createAdminUser(requestDto));
    }

    /**
     * Retrieves all admin users.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AdminUserResponseDto>> getAllAdminUsers() {

        log.info("Get all admin users request received");

        return ResponseEntity.ok(adminUserService.getAllAdminUsers());
    }

    /**
     * Updates an existing admin user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<AdminUserResponseDto> updateAdminUser(
            @PathVariable Long id,
            @RequestBody UpdateAdminUserRequestDto requestDto) {

        log.info("Update admin user request received: id={}", id);

        return ResponseEntity.ok(adminUserService.updateAdminUser(id, requestDto));
    }

    /**
     * Deactivates an existing admin user without deleting the database record.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deactivateAdminUser(
            @PathVariable Long id) {

        log.info("Deactivate admin user request received: id={}", id);
        adminUserService.deactivateAdminUser(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates an existing admin user without deleting the database record.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> activateAdminUser(
            @PathVariable Long id) {

        log.info("Activate admin user request received: id={}", id);
        adminUserService.activateAdminUser(id);

        return ResponseEntity.noContent().build();
    }
}
