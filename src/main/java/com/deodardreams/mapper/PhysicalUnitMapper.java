package com.deodardreams.mapper;


import com.deodardreams.dto.request.CreatePhysicalUnitRequestDto;
import com.deodardreams.dto.request.UpdatePhysicalUnitRequestDto;
import com.deodardreams.dto.response.PhysicalUnitResponseDto;
import com.deodardreams.model.PhysicalUnit;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PhysicalUnitMapper {
    PhysicalUnit toEntity(CreatePhysicalUnitRequestDto request);
    PhysicalUnitResponseDto toResponse(PhysicalUnit entity);

    // Ignores null fields during partial updates so omitted request fields retain their existing values.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    // Updates the existing entity with request values instead of creating a new entity.
    void updatePhysicalUnitEntity(UpdatePhysicalUnitRequestDto requestDto, @MappingTarget PhysicalUnit physicalUnit);
    // @MappingTarget updates the existing entity so its database identity and audit history are preserved.
}
