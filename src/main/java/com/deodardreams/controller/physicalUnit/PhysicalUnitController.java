package com.deodardreams.controller.physicalUnit;

import com.deodardreams.dto.request.CreatePhysicalUnitRequestDto;
import com.deodardreams.dto.request.UpdatePhysicalUnitRequestDto;
import com.deodardreams.dto.response.PhysicalUnitResponseDto;
import com.deodardreams.service.pyhsicalUnit.PhysicalUnitService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/physical-units")
@Slf4j
public class PhysicalUnitController {

    private final PhysicalUnitService physicalUnitService;

    public PhysicalUnitController(PhysicalUnitService physicalUnitService) {
        this.physicalUnitService = physicalUnitService;
    }

    /**
     * Returns all physical units for inventory/room management.
     * Inactive units are also returned because admins need visibility
     * into the complete physical inventory.
     */
    @GetMapping
    public ResponseEntity<List<PhysicalUnitResponseDto>> getAllPhysicalUnits() {

        log.info("Received request to fetch all physical units");

        List<PhysicalUnitResponseDto> physicalUnits =
                physicalUnitService.getAllPhysicalUnits();

        log.info("Successfully fetched {} physical units", physicalUnits.size());

        return ResponseEntity.ok(physicalUnits);
    }

    /**
     * Creates a new physical unit.
     * Returns HTTP 201 when the unit is successfully created.
     */
    @PostMapping
    public ResponseEntity<PhysicalUnitResponseDto> createPhysicalUnit(@Valid @RequestBody CreatePhysicalUnitRequestDto requestDto) {

        log.info("Received request to create a physical unit");

        PhysicalUnitResponseDto response =
                physicalUnitService.createPhysicalUnitAdminOnly(requestDto);

        log.info("Physical unit created successfully with id={}", response.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Updates an existing physical unit using its database identifier.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PhysicalUnitResponseDto> updatePhysicalUnit(@PathVariable Long id, @RequestBody UpdatePhysicalUnitRequestDto requestDto) {

        log.info("Received request to update physical unit with id={}", id);

        PhysicalUnitResponseDto response =
                physicalUnitService.updatePhysicalUnitAdminOnly(id, requestDto);

        log.info("Physical unit with id={} updated successfully", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Soft-deletes a physical unit by marking it inactive.
     * The database record is retained so historical booking information
     * remains intact.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivatePhysicalUnit(@PathVariable Long id) {

        log.info("Received request to deactivate physical unit with id={}", id);
        physicalUnitService.deactivatePhysicalUnitAdminOnly(id);
        log.info("Physical unit with id={} deactivated successfully", id);
        return ResponseEntity.noContent().build();
    }

}
