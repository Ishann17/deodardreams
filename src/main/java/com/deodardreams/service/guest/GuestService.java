package com.deodardreams.service.guest;

import com.deodardreams.dto.request.CreateBookingRequestDto;
import com.deodardreams.model.Guest;

public interface GuestService {

    Guest findOrCreateGuest(CreateBookingRequestDto guestDetails);
}
