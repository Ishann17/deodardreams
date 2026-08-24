package com.deodardreams.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomNotAvailableException extends AppException{

   // private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public RoomNotAvailableException(String message) {
        super(message);
    }
}
