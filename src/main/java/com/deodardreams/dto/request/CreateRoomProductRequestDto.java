package com.deodardreams.dto.request;

/**
 * Data for creating/updating a RoomProduct (pricing/category). Admin-only —
 * guests browse these, but never create or modify them. Access control
 * enforced at the controller/security layer, not here.
 */

import com.deodardreams.enums.RoomCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateRoomProductRequestDto {

    // Identifies the sellable room category.
    @NotNull
    private RoomCategory roomCategory;

    // Display name shown to guests on the website.
    @NotBlank
    private String name;

    // Base price charged before any applicable extra-guest charge.
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basePrice;

    // Additional charge applied when guests exceed base occupancy.
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal extraGuestCharge;

    // Maximum number of guests allowed for this room product.
    @NotNull
    @Positive
    private Integer maxOccupancy;

    // Number of guests included in the base price.
    @NotNull
    @Positive
    private Integer baseOccupancy;
}