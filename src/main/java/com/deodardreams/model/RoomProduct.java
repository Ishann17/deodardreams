package com.deodardreams.model;

/**
 * A sellable product a guest browses and books on the website —
 * e.g. "3 BHK — ₹8000". Maps internally to one or more PhysicalUnit rows,
 * but the guest never sees that mapping.
 */

import com.deodardreams.enums.RoomCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "room_products")
@Getter
@Setter
public class RoomProduct extends BaseEntity{


    // Which product this is — drives fixed allocation rules in the booking logic
    @Enumerated(EnumType.STRING)
    private RoomCategory roomCategory;

    private String name;

    // Price guests pay. BigDecimal, not double — avoids floating-point rounding errors with money
    private BigDecimal basePrice;
    private int maxOccupancy;
}
