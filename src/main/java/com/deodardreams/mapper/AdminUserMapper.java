package com.deodardreams.mapper;

/**
 * Converts between AdminUser (the database entity) and its request/response
 * DTOs, so the service layer never has to manually copy fields one by one.
 * MapStruct reads this interface and writes the actual working code for it
 * automatically, at compile time — nothing in here runs by "magic" at runtime.
 *
 * This class has three jobs, one per method below:
 *   1. toAdminUserEntity      — turn a "create admin" request into a new AdminUser
 *   2. toAdminUserResponse    — turn a saved AdminUser into safe data to send back to the client
 *   3. updateAdminUserEntity  — apply a partial "update admin" request onto an EXISTING AdminUser
 *
 * ANNOTATIONS USED IN THIS FILE, EXPLAINED:
 *
 * @Mapper(componentModel = "spring")
 *   Placed on the interface itself. Tells MapStruct two things: "generate a
 *   real implementation class for this interface" and "register that
 *   generated class as a Spring bean," so it can be injected into services
 *   with @Autowired/constructor injection, just like any other @Service.
 *
 * @Mapping(target = "...", ignore = true)
 *   Placed on individual mapping methods. Says: "do not copy any value into
 *   this specific field on the target object, no matter what." Used here to
 *   protect fields that should NEVER come from a client request directly —
 *   e.g. "id" (the database identity must never change), "passwordHash"
 *   (real password hashing happens separately, in the service layer, never
 *   via direct mapping), and "role"/"isActive" on updates (changing someone's
 *   role or active status is a separate, deliberate action — not something
 *   that should slip in through a general "update my details" request).
 *
 * @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
 *   Placed once, on updateAdminUserEntity only. This is what makes a PARTIAL
 *   update actually partial. Without it, if a client sends an update with
 *   only "name" filled in, every other field (email, phoneNumber) arrives as
 *   null on the DTO — and MapStruct would normally copy those nulls straight
 *   onto the existing AdminUser, silently wiping out real data that was never
 *   meant to change. This annotation says: "if the incoming value is null,
 *   skip it — leave whatever is already saved in the database untouched."
 *
 * @MappingTarget
 *   Placed on the AdminUser parameter of updateAdminUserEntity only. This is
 *   what makes this method fundamentally different from toAdminUserEntity/
 *   toAdminUserResponse. Those two methods each CREATE a brand-new object and
 *   return it. @MappingTarget instead says: "here is an object that already
 *   exists (already fetched from the database) — modify ITS fields directly,
 *   in place, instead of building something new." That's exactly why this
 *   method's return type is void: there is nothing new to hand back, the
 *   object passed in has simply been changed.
 */

import com.deodardreams.dto.request.CreateAdminUserRequestDto;
import com.deodardreams.dto.request.UpdateAdminUserRequestDto;
import com.deodardreams.dto.response.AdminUserResponseDto;
import com.deodardreams.model.AdminUser;
import org.mapstruct.*;

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
    // Applies to the WHOLE mapping method (not one field): whenever a source field
    // (from requestDto) is null, skip it entirely — leave the target's existing value untouched.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

    // The entity's database identity should never change during an update — always skip it.
    @Mapping(target = "id", ignore = true)

    // This DTO has no password field to map from at all (password hashing happens
    // separately in the service layer) — skip it so nothing accidentally overwrites it.
    @Mapping(target = "passwordHash", ignore = true)

    // A profile update should never change someone's role — that's a separate,
    // privileged operation, not something bundled into a general "update my details" call.
    @Mapping(target = "role", ignore = true)

    // Same reasoning as role — activating/deactivating an admin is its own dedicated
    // action (activateAdminUser/deactivateAdminUser), not something this update touches.
    @Mapping(target = "isActive", ignore = true)
    void updateAdminUserEntity(
            UpdateAdminUserRequestDto requestDto,
            @MappingTarget AdminUser adminUser
    );
}
