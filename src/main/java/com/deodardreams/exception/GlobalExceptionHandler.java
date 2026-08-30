package com.deodardreams.exception;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException exception, HttpServletRequest request){
        log.warn("Resource not found on path {}: {}", request.getRequestURI(), exception.getMessage());

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "RESOURCE_NOT_FOUND");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.warn("Validation failed on path {}: {}", request.getRequestURI(), exception.getMessage());
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "VALIDATION_FAILED");
        errorResponse.put("message", "Request validation failed");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("fieldErrors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<?> handleRoomNotAvailable(RoomNotAvailableException exception, HttpServletRequest request) {
        log.warn("Room not available on path {}: {}", request.getRequestURI(), exception.getMessage());
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "ROOM_NOT_AVAILABLE");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MaxOccupancyExceededException.class)
    public ResponseEntity<?> handleMaxOccupancyExceeded(MaxOccupancyExceededException exception, HttpServletRequest request) {
        log.warn("Maximum Occupancy Exceeded {}: {}", request.getRequestURI(), exception.getMessage());
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "MAX_OCCUPANCY_EXCEEDED");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request){
        log.warn("Access denied on path {}: {}", request.getRequestURI(), exception.getMessage());
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "ACCESS_DENIED");
        errorResponse.put("message", "You do not have permission to perform this action.");
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    //Fall back method for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception on path {}: ", request.getRequestURI(), exception);
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "INTERNAL_SERVER_ERROR");
        errorResponse.put("message", "Something went wrong. Please try again later."); // never expose exception.getMessage() here
        errorResponse.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
