package com.deodardreams.exception;

/**
 * Thrown when a guest tries to book a physical unit/room product that's
 * already reserved for an overlapping date range. Distinct from
 * ResourceNotFoundException — the room exists, it's just unavailable
 * for the requested dates. Converted into an HTTP 409 Conflict.
 */

public class RoomNotAvailableException extends RuntimeException{

    public RoomNotAvailableException (String message){
        super(message);
    }
}
