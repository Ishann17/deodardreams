package com.deodardreams.mapper;

import com.deodardreams.dto.request.CreateBookingRequestDto;
import com.deodardreams.dto.response.GuestResponseDto;
import com.deodardreams.model.Guest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GuestMapper {

    Guest toGuestEntity(CreateBookingRequestDto requestDto);
    GuestResponseDto toGuestResponse(Guest guestDetails);
}
