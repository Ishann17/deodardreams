package com.deodardreams.exception;

/**
 * Base class for all custom exceptions in this application.
 * Lets a single @ExceptionHandler catch every app-specific error
 * without also catching unrelated framework exceptions.
 */

public abstract class AppException extends RuntimeException{

    public AppException (String message){
        super(message);
    }
}
