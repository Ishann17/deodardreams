package com.deodardreams.model;

/**
 * Represents a person who submits an enquiry before making a booking.
 *
 * This entity stores the guest's contact details along with the requirements
 * of that particular enquiry, such as rooms, adults, children and stay dates.
 *
 * An enquiry may later be converted into an actual Guest + Booking.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "enquiry_users")
@Getter
@Setter
public class EnquiryUser extends BaseEntity{

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String mobile;

    @Column(nullable = false)
    private String email;

    private Integer numberOfRooms;

    private Integer numberOfAdults;

    // Number of children below 12 years of age.

    private Integer childrenBelow12;

    // Optional because the guest may enquire without having fixed dates.
    private LocalDate checkIn;

    // Optional because the guest may enquire without having fixed dates.
    private LocalDate checkOut;

    // Additional requirement or question entered by the guest.
    @Column(length = 2000)
    private String enquiryMessage;

}
