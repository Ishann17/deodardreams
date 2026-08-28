package com.deodardreams.repository;

import com.deodardreams.enums.RoomCategory;
import com.deodardreams.model.RoomProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomProductRepository extends JpaRepository<RoomProduct, Long> {

    // Looks up the single sellable room product by its category during booking to retrieve its pricing and occupancy rules.
    Optional<RoomProduct> findByRoomCategory(RoomCategory roomCategory);
}
