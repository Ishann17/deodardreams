package com.deodardreams.dto.request;
/**
 * Data for creating/updating a RoomProduct (pricing/category). Admin-only —
 * guests browse these, but never create or modify them. Access control
 * enforced at the controller/security layer, not here.
 */

import com.deodardreams.enums.RoomCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateRoomProductRequestDto {

    @NotNull
    private RoomCategory roomCategory;

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    @NotNull
    @Positive
    private Integer maxOccupancy;
}