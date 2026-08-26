package com.deodardreams.service.pyhsicalUnit;

import com.deodardreams.dto.request.CreatePhysicalUnitRequestDto;
import com.deodardreams.dto.request.UpdatePhysicalUnitRequestDto;
import com.deodardreams.dto.response.PhysicalUnitResponseDto;
import com.deodardreams.exception.ResourceNotFoundException;
import com.deodardreams.mapper.PhysicalUnitMapper;
import com.deodardreams.model.PhysicalUnit;
import com.deodardreams.repository.PhysicalUnitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PhysicalUnitServiceImpl implements PhysicalUnitService{

    private final PhysicalUnitRepository physicalUnitRepository;
    private final PhysicalUnitMapper physicalUnitMapper;

    public PhysicalUnitServiceImpl(PhysicalUnitRepository physicalUnitRepository, PhysicalUnitMapper physicalUnitMapper) {
        this.physicalUnitRepository = physicalUnitRepository;
        this.physicalUnitMapper = physicalUnitMapper;
    }

    @Override
    public List<PhysicalUnitResponseDto> getAllPhysicalUnits() {
        log.info("Fetching all physical units");
        List<PhysicalUnit> physicalUnitList = physicalUnitRepository.findAll();
        log.info("Fetched {} physical units", physicalUnitList.size());
        return physicalUnitList.stream().map(physicalUnitMapper::toResponse).toList();
    }

    @Override
    public PhysicalUnitResponseDto createPhysicalUnitAdminOnly(CreatePhysicalUnitRequestDto requestDto) {
        log.info("Creating new physical unit");
        PhysicalUnit entity = physicalUnitMapper.toEntity(requestDto);
        PhysicalUnit savedPhysicalUnit = physicalUnitRepository.save(entity);
        log.info("Physical unit created successfully with id={}",savedPhysicalUnit.getId());
        return physicalUnitMapper.toResponse(savedPhysicalUnit);
    }

    @Override
    public PhysicalUnitResponseDto updatePhysicalUnitAdminOnly(Long id, UpdatePhysicalUnitRequestDto requestDto) {
        // Fetches the existing physical unit so the requested changes are applied to the same database record.
        log.info("Updating physical unit with id={}", id);
        PhysicalUnit physicalUnit = physicalUnitRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Physical Unit by id " + id + " is not available"));

        // Updates the existing entity using the request values without creating a new physical unit.
        physicalUnitMapper.updatePhysicalUnitEntity(requestDto, physicalUnit);
        // Persists the modified entity back to the database.
        PhysicalUnit updatedPhysicalUnit =  physicalUnitRepository.save(physicalUnit);

        log.info("Physical unit with id={} updated successfully", id);

        return physicalUnitMapper.toResponse(updatedPhysicalUnit);
    }

    @Override
    public void deactivatePhysicalUnitAdminOnly(Long physicalUnitId) {
        log.info("Deactivating physical unit with id={}", physicalUnitId);
        PhysicalUnit physicalUnit = physicalUnitRepository.findById(physicalUnitId).orElseThrow(() -> new ResourceNotFoundException("Physical Unit by id " + physicalUnitId + " is not available"));

        if(!physicalUnit.getIsActive()){
            log.info("Physical unit with id={} is already inactive",physicalUnitId);
            return;
        }

        physicalUnit.setIsActive(false);
        physicalUnitRepository.save(physicalUnit);
        log.info("Physical unit with id={} deactivated successfully",physicalUnitId);
    }
}
