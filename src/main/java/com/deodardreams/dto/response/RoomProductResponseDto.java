package com.deodardreams.dto.response;

/**
 * What the API returns when showing a RoomProduct — used both on the
 * public-facing browse page and the admin management screen.
 */

import com.deodardreams.enums.RoomCategory;

import java.math.BigDecimal;

public record RoomProductResponseDto(
        Long id,
        RoomCategory roomCategory,
        String name,
        BigDecimal basePrice,
        BigDecimal extraGuestCharge,
        int baseOccupancy,
        int maxOccupancy
) {}
