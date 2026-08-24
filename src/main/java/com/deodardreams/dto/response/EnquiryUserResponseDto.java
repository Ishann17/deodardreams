package com.deodardreams.dto.response;

/**
 * Data returned after an enquiry is submitted successfully.
 *
 * Represents the saved enquiry without exposing persistence-only audit fields.
 */

import java.time.LocalDate;

public record EnquiryUserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String mobile,
        String email,
        Integer numberOfRooms,
        Integer numberOfAdults,
        Integer childrenBelow12,
        LocalDate checkIn,
        LocalDate checkOut,
        String enquiryMessage
) {}