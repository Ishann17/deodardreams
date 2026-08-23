package com.deodardreams.enums;

public enum BookingStatus {
    PENDING,     // created, awaiting payment
    CONFIRMED,   // payment succeeded, booking is locked in
    CANCELLED,   // guest or admin cancelled
    COMPLETED    // stay has finished (check-out date passed)
}
