package com.deodardreams.mapper;
/**
 * Maps Booking entity to BookingResponseDto for all booking-related API responses.
 */

import com.deodardreams.dto.response.BookingResponseDto;
import com.deodardreams.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "guestName", source = "guest.firstName")
    @Mapping(target = "roomProductName", source = "roomProduct.name")
    // It tells MapStruct to call that method (calculateNights) and put the result into the DTO's numberOfNights
    @Mapping(target = "numberOfNights", source = "booking", qualifiedByName = "calculateNights")
    // status (enum) auto-converts to String — MapStruct calls .name() by default, no hint needed
    BookingResponseDto toBookingResponseDto(Booking booking);

    /**
     * Calculates the number of nights the guest stays: checkOut minus checkIn.
     *
     * WHY THIS EXISTS:
     * The API response (BookingResponseDto) needs "numberOfNights", but the Booking
     * entity has no such field. We deliberately did NOT add one, because nights is
     * derived data. Storing it would duplicate what checkIn and checkOut already say,
     * and the values could drift out of sync if the dates are ever edited.
     * So we compute it here, at mapping time, every time a response is built.
     *
     * HOW MAPSTRUCT USES IT:
     * toBookingResponseDto() points at this method via
     * @Mapping(target = "numberOfNights", source = "booking", qualifiedByName = "calculateNights").
     * We pass the whole Booking (not a single field) because we need two of its
     * fields: checkIn and checkOut.
     *
     */
    @Named("calculateNights")
    default Long calculateNights(Booking booking) {
        if (booking.getCheckIn() == null || booking.getCheckOut() == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
    }
}