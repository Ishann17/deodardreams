package com.deodardreams.repository;

import com.deodardreams.enums.BookingStatus;
import com.deodardreams.model.BookingUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingUnitRepository extends JpaRepository<BookingUnit, Long> {

    /**
     * Finds existing reservations that CLASH with the dates a guest wants, for one room.
     *
     * Empty list = the room is free for those dates. Non-empty = the room is taken.
     *
     * JPQL note: this query is written against our Java classes and fields
     * (BookingUnit, checkIn), not database table and column names. Hibernate
     * turns it into SQL and joins the related tables automatically.
     *
     * A reservation clashes when ALL of these are true:
     *   1. it is for the requested room,
     *   2. the existing guest arrives BEFORE the new guest leaves,
     *   3. the existing guest leaves AFTER the new guest arrives,
     *   4. its booking status is NOT one that frees the room
     *      (see BookingStatus.freedUpRoomStatuses(): CANCELLED, EXPIRED).
     */
    @Query("SELECT bu FROM BookingUnit bu WHERE bu.physicalUnit.id = :physicalUnitId " +
            "AND bu.checkIn < :requestedCheckOut AND bu.checkOut > :requestedCheckIn " +
            "AND bu.booking.status NOT IN :excludedStatuses")
    List<BookingUnit> findOverlappingBookings(
            @Param("physicalUnitId") Long physicalUnitId,
            @Param("requestedCheckIn") LocalDate requestedCheckIn,
            @Param("requestedCheckOut") LocalDate requestedCheckOut,
            @Param("excludedStatuses") List<BookingStatus> excludedStatuses
    );
}
// HOW THIS QUERY WORKS
// (JPQL = a query written with our Java class and field names, not database table names.
//  Hibernate turns it into real SQL and joins the tables for us.)
//
// SELECT bu FROM BookingUnit bu
//    Look through all BookingUnit objects. Each one means "one room reserved for one booking".
//    "bu" is just a short nickname for a BookingUnit.
//
// WHERE bu.physicalUnit.id = :physicalUnitId
//    Keep only the reservations of the room we are asking about.
//    ":physicalUnitId" is a blank slot, filled with the method argument when the query runs.
//
// AND bu.checkIn < :requestedCheckOut
//    ...and the existing guest arrives BEFORE the new guest leaves,
//
// AND bu.checkOut > :requestedCheckIn
//    ...and the existing guest leaves AFTER the new guest arrives.
//    (These two lines together mean "the two stays overlap".)
//
// AND bu.booking.status NOT IN :excludedStatuses
//    ...and the booking's status is NOT in the "ignore" list (CANCELLED, EXPIRED).
//    "bu.booking.status" follows the link from the BookingUnit to its Booking and
//    reads that Booking's status. Hibernate adds the table join for us.
//
// A reservation is returned only if ALL the conditions are true (that is what AND means).
// Result: the reservations that clash with the requested dates.
// An empty result means the room is free.