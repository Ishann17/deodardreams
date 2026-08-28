package com.deodardreams.controller.roomproduct;

import com.deodardreams.dto.request.CreateRoomProductRequestDto;
import com.deodardreams.dto.response.RoomProductResponseDto;
import com.deodardreams.service.roomProduct.RoomProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-products")
@Slf4j
public class RoomProductController {
    private final RoomProductService roomProductService;

    public RoomProductController(RoomProductService roomProductService) {
        this.roomProductService = roomProductService;
    }

    /**
     * Returns all room products available in the system.
     */
    @GetMapping
    public ResponseEntity<List<RoomProductResponseDto>> getAllRoomProducts() {

        log.info("Received request to fetch all room products");

        List<RoomProductResponseDto> roomProducts =
                roomProductService.getAllRoomProducts();

        log.info("Successfully fetched {} room products", roomProducts.size());

        return ResponseEntity.ok(roomProducts);
    }

    /**
     * Creates a new room product for the property.
     * Returns HTTP 201 when the product is successfully created.
     */
    @PostMapping
    public ResponseEntity<RoomProductResponseDto> createRoomProduct(
            @RequestBody CreateRoomProductRequestDto requestDto) {

        log.info("Received request to create a new room product");

        RoomProductResponseDto response =
                roomProductService.createRoomProductAdminOnly(requestDto);

        log.info("Room product created successfully with id={}", response.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Updates an existing room product.
     * Only the supplied values are applied to the existing product,
     * depending on the mapper's null-value update configuration.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RoomProductResponseDto> updateRoomProduct(
            @PathVariable Long id,
            @RequestBody CreateRoomProductRequestDto requestDto) {

        log.info("Received request to update room product with id={}", id);

        RoomProductResponseDto response =
                roomProductService.updateRoomProductAdminOnly(id, requestDto);

        log.info("Room product with id={} updated successfully", id);

        return ResponseEntity.ok(response);
    }
}
