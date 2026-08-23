package com.deodardreams.model;
/**
 * Represents a physical, bookable space in the property —
 * an individual room, a full 2BHK unit, a bedroom within a 2BHK.
 */

import com.deodardreams.enums.UnitType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "physical_units")
@Getter
@Setter
public class PhysicalUnit extends BaseEntity {

    private String floor;
    @Enumerated(EnumType.STRING)
    private UnitType unitType;  // Type of physical space: individual room, hall, full 2BHK unit, or a bedroom within one
    private Long parentUnitId; // // Points to the parent 2BHK unit's id if this row is a bedroom within one; NULL for standalone rooms
    private String name;
    private int capacity;
    private boolean isActive;

}
