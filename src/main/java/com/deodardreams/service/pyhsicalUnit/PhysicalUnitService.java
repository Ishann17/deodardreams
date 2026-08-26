package com.deodardreams.service.pyhsicalUnit;

import com.deodardreams.dto.request.CreatePhysicalUnitRequestDto;
import com.deodardreams.dto.request.UpdatePhysicalUnitRequestDto;
import com.deodardreams.dto.response.PhysicalUnitResponseDto;

import java.util.List;

public interface PhysicalUnitService {

    List<PhysicalUnitResponseDto> getAllPhysicalUnits();
    PhysicalUnitResponseDto createPhysicalUnitAdminOnly(CreatePhysicalUnitRequestDto requestDto);
    PhysicalUnitResponseDto updatePhysicalUnitAdminOnly(Long id, UpdatePhysicalUnitRequestDto requestDto);
    void deactivatePhysicalUnitAdminOnly(Long id);
}
