package com.deodardreams.dto.request;

import com.deodardreams.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // No @NotNull needed — primitive boolean defaults to false if omitted, which is a safe default
    private Boolean isActive;
}
