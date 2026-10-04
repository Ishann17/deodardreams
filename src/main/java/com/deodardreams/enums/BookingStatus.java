package com.deodardreams.enums;

import java.util.List;

public enum BookingStatus {
    PENDING,     // created, awaiting payment
    CONFIRMED,   // payment succeeded, booking is locked in
    CANCELLED,   // BOOKING CANCELLED = cancelled on purpose by Admin
    EXPIRED,    // Not paid within 1 hour, so the system released the room
    COMPLETED;    // stay has finished (check-out date passed).

    // A booking in any of these statuses no longer holds its room, so the room is free to book
    public static List<BookingStatus> freedUpRoomStatuses(){
        return List.of(CANCELLED, EXPIRED);
    }
}
