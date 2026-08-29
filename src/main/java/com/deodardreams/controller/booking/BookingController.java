package com.deodardreams.controller.booking;

import com.deodardreams.dto.request.CreateBookingRequestDto;
import com.deodardreams.dto.response.BookingResponseDto;
import com.deodardreams.service.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
    @GetMapping("/cancelled")
    public ResponseEntity<List<BookingResponseDto>> getAllCancelledBookings() {

        List<BookingResponseDto> cancelledBookings = bookingService.getAllCancelledBookings();

        return ResponseEntity.ok(cancelledBookings);
    }

    @GetMapping("/confirmed")
    public ResponseEntity<List<BookingResponseDto>> getAllConfirmedBookings() {

        List<BookingResponseDto> confirmedBookings = bookingService.getAllConfirmedBooking();

        return ResponseEntity.ok(confirmedBookings);
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody CreateBookingRequestDto request) {
        BookingResponseDto response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}