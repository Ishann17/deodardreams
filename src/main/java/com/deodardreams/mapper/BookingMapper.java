package com.deodardreams.mapper;
/**
 * Maps Booking entity to BookingResponseDto for all booking-related API responses.
 */

import com.deodardreams.dto.response.BookingResponseDto;
import com.deodardreams.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "guestName", source = "guest.firstName")
    @Mapping(target = "roomProductName", source = "roomProduct.name")
    // status (enum) auto-converts to String — MapStruct calls .name() by default, no hint needed
    BookingResponseDto toBookingResponseDto(Booking booking);
}