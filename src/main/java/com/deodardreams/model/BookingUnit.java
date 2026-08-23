package com.deodardreams.model;
/**
 * Links one Booking to one PhysicalUnit it occupies, for a date range.
 * A Single Suite booking creates 1 row here. A 3 BHK booking creates 3 rows
 * (one per room). This is the table availability/double-booking checks run against.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "booking_units")
public class BookingUnit extends BaseEntity {

    // Which reservation this room-assignment belongs to.
    // ManyToOne (not raw Long) so we can navigate to guest/product details from here, e.g. for admin screens.
    // LAZY — availability checks scan this table constantly and only need physicalUnit_id + dates,
    // not the full Booking object, so we avoid loading it unless something explicitly asks for it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Which actual room/unit is reserved. Same ManyToOne + LAZY reasoning as above.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physical_unit_id", nullable = false)
    private PhysicalUnit physicalUnit;

    // Duplicated from Booking's dates on purpose (not sloppy denormalization) — lets the
    // availability check run as a single simple query against this table alone:
    // "is this physical_unit_id free between these dates" — without joining back to Booking.
    private LocalDate checkIn;
    private LocalDate checkOut;
}