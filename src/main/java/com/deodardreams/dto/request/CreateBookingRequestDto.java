package com.deodardreams.dto.request;

/**
 * Data submitted by a guest to create a booking — name, contact info,
 * which room product, and the stay dates. The price is NEVER taken from
 * this DTO — it's always calculated server-side from RoomProduct.basePrice.
 *
 * WHICH TABLES THIS TRIGGERS (not 1-to-1 — this DTO drives a multi-table flow):
 *
 * 1. GUESTS table — firstName/lastName/email/phoneNumber/city/state feed this.
 *    NOT always an INSERT — the service layer first checks if a Guest with this
 *    phoneNumber already exists (repeat guest) and reuses that row if so.
 *
 * 2. ROOM_PRODUCTS table — roomProductCode is used to LOOK UP an existing row
 *    here (never inserted/created by this DTO), to get the real basePrice and
 *    confirm the product actually exists.
 *
 * 3. BOOKINGS table — a new row is always created here: guest_id (from step 1),
 *    room_product_id (from step 2), checkIn, checkOut, totalAmount (calculated
 *    server-side from basePrice, never from client input), status = PENDING.
 *
 * 4. BOOKING_UNITS table — NOT directly from any field in this DTO. Once the
 *    Booking row exists, the service layer runs the room-allocation logic
 *    (e.g. THREE_BHK → Rooms 1,2,3 fixed) and inserts one row here per
 *    physical room assigned — this is where availability/double-booking
 *    checks actually happen.
 *
 * A 5th table, PAYMENTS, gets its first row (status = CREATED) right after
 * this, once the Razorpay order is created — also not driven by this DTO
 * directly, but the very next step in the same overall flow.
 */

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateBookingRequestDto {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain letters only")
    String firstName;
    @NotBlank
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain letters only")
    String lastName;

    @NotBlank
    @Email
    String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
    String phoneNumber;
    String city;
    String state;

    @NotNull
    @FutureOrPresent
    LocalDate checkIn;
    @NotNull
    @Future
    LocalDate checkOut;

    // Which product is being booked — e.g. "THREE_BHK". Required to know what the guest wants.
    @NotBlank
    private String roomProductCode;

}
