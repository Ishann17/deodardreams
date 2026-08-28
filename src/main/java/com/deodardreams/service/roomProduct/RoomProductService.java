package com.deodardreams.service.roomProduct;

import com.deodardreams.dto.request.CreateRoomProductRequestDto;
import com.deodardreams.dto.response.RoomProductResponseDto;

import java.util.List;

public interface RoomProductService {
    List<RoomProductResponseDto> getAllRoomProducts();

    RoomProductResponseDto createRoomProductAdminOnly(
            CreateRoomProductRequestDto requestDto);

    RoomProductResponseDto updateRoomProductAdminOnly(
            Long id,
            CreateRoomProductRequestDto requestDto);
}
