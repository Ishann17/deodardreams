package com.deodardreams.dto.response;

/**
 * Data returned after an enquiry is submitted successfully.
 *
 * Represents the saved enquiry without exposing persistence-only audit fields.
 */

import java.time.LocalDate;

public record EnquiryUserResponseDto(
        String message
) {
}