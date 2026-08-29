package com.deodardreams.mapper;

import com.deodardreams.dto.request.CreateAdminUserRequestDto;
import com.deodardreams.dto.request.UpdateAdminUserRequestDto;
import com.deodardreams.dto.response.AdminUserResponseDto;
import com.deodardreams.model.AdminUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    AdminUser toAdminUserEntity(CreateAdminUserRequestDto requestDto);

    AdminUserResponseDto toAdminUserResponse(AdminUser adminUser);

    /**
     * Updates the existing admin user entity with the fields provided in the request.
     *
     * The ID and passwordHash are intentionally ignored because the ID identifies
     * the existing record, while password changes should be handled separately
     * through PasswordEncoder in the service layer.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateAdminUserEntity(
            UpdateAdminUserRequestDto requestDto,
            @MappingTarget AdminUser adminUser
    );
}
