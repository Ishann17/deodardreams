package com.deodardreams.repository;

import com.deodardreams.enums.UnitType;
import com.deodardreams.model.PhysicalUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhysicalUnitRepository extends JpaRepository<PhysicalUnit, Long> {

    /**
     * Finds all active physical units of the requested type.
     * Used during booking to identify physical units that can potentially
     * be allocated before checking their date availability.
     */
    List<PhysicalUnit> findByUnitTypeAndIsActiveTrue(UnitType unitType);

    /**
     * Finds active 1 BHK bedrooms belonging to the specified 2 BHK parent unit.
     * Used during 1 BHK allocation to keep multiple 1 BHK bookings within
     * the same 2 BHK unit whenever enough bedrooms are available.
     */
    List<PhysicalUnit> findByParentUnitIdAndUnitTypeAndIsActiveTrue(
            Long parentUnitId,
            UnitType unitType
    );
}
