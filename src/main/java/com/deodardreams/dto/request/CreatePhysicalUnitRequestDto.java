package com.deodardreams.dto.request;
/**
 * Data for creating/updating a PhysicalUnit — admin-only operation
 * (room/unit setup is not guest-facing). Access control enforced at the
 * controller/security layer, not here — this DTO only validates shape.
 */

import com.deodardreams.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePhysicalUnitRequestDto {

    @NotBlank
    private String floor;

    @NotNull
    private UnitType unitType;

    // Optional — only set when this unit is a bedroom within a 2BHK. Null = standalone unit.
    private Long parentUnitId;

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Integer capacity;

    // No @NotNull needed — primitive boolean defaults to false if omitted, which is a safe default
    private boolean isActive;
}