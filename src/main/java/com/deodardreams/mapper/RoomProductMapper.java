package com.deodardreams.mapper;

import com.deodardreams.dto.request.CreateRoomProductRequestDto;
import com.deodardreams.dto.response.RoomProductResponseDto;
import com.deodardreams.model.RoomProduct;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoomProductMapper {
    RoomProduct toRoomProductEntity(CreateRoomProductRequestDto requestDto);
    RoomProductResponseDto toRoomProductResponse(RoomProduct roomProduct);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
// Updates only the values provided in the request and preserves existing values for null fields.
    void updateRoomProductEntity(
            CreateRoomProductRequestDto requestDto,
            @MappingTarget RoomProduct roomProduct
    );
}
