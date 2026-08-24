package com.deodardreams.dto.response;

/**
 * What the API sends back after a booking is successfully created.
 * Immutable by design (record) — this is a fixed snapshot of the result,
 * never meant to be modified after it's built.
 */

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponseDto(Long bookingId,
                                 String guestName,
                                 String roomProductName,
                                 LocalDate checkIn,
                                 LocalDate checkOut,
                                 BigDecimal totalAmount,
                                 String status)
{}
