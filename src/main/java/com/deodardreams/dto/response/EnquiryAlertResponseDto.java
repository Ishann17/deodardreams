package com.deodardreams.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EnquiryAlertResponseDto {

    private Long id;

    private String firstName;

    private String lastName;

    private String mobile;

    private String email;

    @Positive
    private Integer numberOfRooms;

    @Positive
    private Integer numberOfAdults;

    @PositiveOrZero
    private Integer childrenBelow12;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate checkIn;


    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate checkOut;

    private String enquiryMessage;
}
