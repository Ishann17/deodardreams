package com.deodardreams.service.roomProduct;

import com.deodardreams.dto.request.CreateRoomProductRequestDto;
import com.deodardreams.dto.response.RoomProductResponseDto;
import com.deodardreams.exception.ResourceNotFoundException;
import com.deodardreams.mapper.RoomProductMapper;
import com.deodardreams.model.RoomProduct;
import com.deodardreams.repository.RoomProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RoomProductServiceImpl implements RoomProductService{

    private final RoomProductRepository roomProductRepository;
    private final RoomProductMapper roomProductMapper;

    public RoomProductServiceImpl(RoomProductRepository roomProductRepository, RoomProductMapper roomProductMapper) {
        this.roomProductRepository = roomProductRepository;
        this.roomProductMapper = roomProductMapper;
    }

    @Override
    public List<RoomProductResponseDto> getAllRoomProducts() {
        log.info("Fetching all room products");
        List<RoomProduct> roomProductList = roomProductRepository.findAll();

        return roomProductList.stream().map(roomProductMapper::toRoomProductResponse).toList();
    }

    @Override
    public RoomProductResponseDto createRoomProductAdminOnly(CreateRoomProductRequestDto requestDto) {
        log.info("Creating new room product");
        RoomProduct roomProduct = roomProductRepository.save(roomProductMapper.toRoomProductEntity(requestDto));

        return roomProductMapper.toRoomProductResponse(roomProduct);
    }

    @Override
    public RoomProductResponseDto updateRoomProductAdminOnly(Long id, CreateRoomProductRequestDto requestDto) {
        log.info("Updating room product with id={}", id);
        RoomProduct roomProduct = roomProductRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room Product with id " + id + " is not available."));

        //Update the entity with the changed fields in request
        roomProductMapper.updateRoomProductEntity(requestDto, roomProduct);
        RoomProduct updatedRoomProduct = roomProductRepository.save(roomProduct);

        return roomProductMapper.toRoomProductResponse(updatedRoomProduct);
    }
}
