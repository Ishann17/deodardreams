package com.deodardreams.service.admin;

import com.deodardreams.dto.request.CreateAdminUserRequestDto;
import com.deodardreams.dto.request.UpdateAdminUserRequestDto;
import com.deodardreams.dto.response.AdminUserResponseDto;
import com.deodardreams.model.AdminUser;

import java.util.List;

public interface AdminUserService {

    AdminUserResponseDto createAdminUser(CreateAdminUserRequestDto requestDto);

    List<AdminUserResponseDto> getAllAdminUsers();

    AdminUserResponseDto updateAdminUser(
            Long id,
            UpdateAdminUserRequestDto requestDto
    );

    void deactivateAdminUser(Long id);
    void activateAdminUser(Long id);
}
