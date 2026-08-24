package com.deodardreams.repository;

import com.deodardreams.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByPhoneNumber(String phoneNumber);
}
