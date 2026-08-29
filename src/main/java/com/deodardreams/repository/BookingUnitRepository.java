package com.deodardreams.repository;

import com.deodardreams.enums.BookingStatus;
import com.deodardreams.model.BookingUnit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingUnitRepository extends JpaRepository<BookingUnit, Long> {

    /**
     * Finds existing booking-unit allocations that overlap the requested stay dates
     * for a given physical unit. Two date ranges overlap unless one entirely ends
     * before the other begins — this query expresses exactly that condition.
     */
    @Query("SELECT bu FROM BookingUnit bu WHERE bu.physicalUnit.id = :physicalUnitId " +
            "AND bu.checkIn < :requestedCheckOut AND bu.checkOut > :requestedCheckIn " +
            "AND bu.booking.status <> :excludedStatus")
    List<BookingUnit> findOverlappingBookings(
            @Param("physicalUnitId") Long physicalUnitId,
            @Param("requestedCheckIn") LocalDate requestedCheckIn,
            @Param("requestedCheckOut") LocalDate requestedCheckOut,
            @Param("excludedStatus") BookingStatus excludedStatus
    );
}
