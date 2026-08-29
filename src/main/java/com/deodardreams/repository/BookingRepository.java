package com.deodardreams.repository;

import com.deodardreams.enums.BookingStatus;
import com.deodardreams.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByStatusIn(List<BookingStatus> statuses);
}
