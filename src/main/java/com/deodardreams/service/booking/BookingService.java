package com.deodardreams.service.booking;

import com.deodardreams.dto.request.CreateBookingRequestDto;
import com.deodardreams.dto.response.BookingResponseDto;

public interface BookingService {
    public BookingResponseDto createBooking(CreateBookingRequestDto requestDto);
}
