package com.deodardreams.dto.request;

/**
 * Request used to partially update an existing physical unit.
 * Only fields provided in the request are updated; omitted fields remain unchanged.
 */

import com.deodardreams.enums.UnitType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePhysicalUnitRequestDto {

    private String floor;

    private UnitType unitType;

    private Long parentUnitId;

    private String name;

    // Nullable so null/omitted means the existing active state should remain unchanged.
    private Boolean isActive;
}
