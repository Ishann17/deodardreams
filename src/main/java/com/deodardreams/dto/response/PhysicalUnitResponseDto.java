package com.deodardreams.dto.response;

/**
 * What the API returns when showing a PhysicalUnit — e.g. in the admin
 * room-management screen. Immutable snapshot (record).
 */

import com.deodardreams.enums.UnitType;

public record PhysicalUnitResponseDto(
        Long id,
        String floor,
        UnitType unitType,
        Long parentUnitId,
        String name,
        Boolean isActive
) {}
