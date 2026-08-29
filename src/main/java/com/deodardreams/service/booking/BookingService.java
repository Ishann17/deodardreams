package com.deodardreams.service.booking;

import com.deodardreams.dto.request.CreateBookingRequestDto;
import com.deodardreams.dto.response.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(CreateBookingRequestDto requestDto);
    BookingResponseDto cancelBooking (Long bookingId);
    List<BookingResponseDto> getAllBookings();
}
