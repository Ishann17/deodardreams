package com.deodardreams.model;

/**
 * A single reservation — a guest booking a room product for a date range.
 * Does not record which specific physical rooms were assigned — see BookingUnit for that.
 */

import com.deodardreams.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking extends BaseEntity{

    // Many bookings can belong to the same guest (repeat stays) — ManyToOne, not raw Long,
    // so we can navigate booking.getGuest() directly for admin dashboards / repeat-guest insights.
    // Nullable=false — every booking must belong to a guest.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    // Many bookings can be for the same product (e.g. many guests book THREE_BHK over time) —
    // ManyToOne lets us show product name/price directly without a manual lookup.
    // Which specific physical rooms get assigned for this booking is decided separately (see BookingUnit).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_product_id", nullable = false)
    private RoomProduct roomProduct;

    private LocalDate checkIn;
    private LocalDate checkOut;

    // What the guest owes. BigDecimal — same reasoning as RoomProduct.basePrice.
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

// createdBy (inherited from BaseEntity) stays NULL for guest self-service bookings.
// It's only populated when an admin/employee creates a booking manually on the guest's behalf —
// the guest field above always points to the actual stayer either way.
// See BaseEntity for the general audit-field explanation.
}
