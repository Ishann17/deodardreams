package com.deodardreams.repository;

import com.deodardreams.enums.UnitType;
import com.deodardreams.model.PhysicalUnit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Same as findByUnitTypeAndIsActiveTrue, but with a pessimistic write lock
     * (PESSIMISTIC_WRITE → generates SQL's "FOR UPDATE" clause).
     *
     * This must be used instead of the plain version anywhere availability is
     * being checked as part of creating a booking. Locking these rows means any
     * other transaction trying to run this same query against the same unit type
     * has to WAIT until this transaction commits or rolls back — closing the gap
     * between "check if a room is free" and "actually reserve it" that would
     * otherwise let two concurrent requests both see the same room as available
     * and double-book it.
     *
     * Only meaningful inside an active @Transactional method — the lock is held
     * for the duration of the transaction and released the moment it ends.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pu FROM PhysicalUnit pu WHERE pu.unitType = :unitType AND pu.isActive = true")
    List<PhysicalUnit> findByUnitTypeAndIsActiveTrueForUpdate(@Param("unitType") UnitType unitType);
}
