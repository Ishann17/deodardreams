package com.deodardreams.dto.request;

/**
 * Data submitted by a visitor to enquire about a stay.
 *
 * Contains contact details, occupancy requirements and optional stay dates.
 * This DTO is only for enquiry input; it does not create a Booking.
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CreateEnquiryUserRequestDto {

    @NotBlank
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Name must contain letters only"
    )
    private String firstName;

    private String lastName;

    @NotBlank
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Mobile number must be a valid 10-digit Indian mobile number"
    )
    private String mobile;

    @NotBlank
    @Email
    private String email;

    @Positive
    private Integer numberOfRooms;

    @Positive
    private Integer numberOfAdults;

    @PositiveOrZero
    private Integer childrenBelow12;

    // Optional because the visitor may enquire without fixed stay dates.
    @FutureOrPresent
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate checkIn;

    // Optional because the visitor may enquire without fixed stay dates.
    @Future
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate checkOut;

    // Optional additional question or requirement from the visitor.
    @Size(max = 2000)
    private String enquiryMessage;
}