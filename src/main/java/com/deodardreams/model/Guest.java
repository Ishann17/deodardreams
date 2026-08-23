package com.deodardreams.model;

/**
 * A person who books a stay. No login/account required — guests fill a form
 * at checkout, and the backend matches or creates a record here by phone number.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "guests")
@Getter
@Setter
public class Guest extends BaseEntity{

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    private String city;
    private String state;
    @Column(unique = true)
    @Email
    private String email;
    @Column(unique = true, nullable = false)
    private String phoneNumber;

}
