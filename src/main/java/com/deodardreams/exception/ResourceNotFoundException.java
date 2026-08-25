package com.deodardreams.exception;

/**
 * Thrown when the requested resource does not exist.
 * The global exception handler converts this into an HTTP 404 response.
 */

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message){
        super(message);
    }
}
