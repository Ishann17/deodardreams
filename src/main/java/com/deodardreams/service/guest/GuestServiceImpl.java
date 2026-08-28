package com.deodardreams.service.guest;


import com.deodardreams.dto.request.CreateBookingRequestDto;
import com.deodardreams.mapper.GuestMapper;
import com.deodardreams.model.Guest;
import com.deodardreams.repository.GuestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class GuestServiceImpl implements GuestService{

    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    public GuestServiceImpl(GuestRepository guestRepository, GuestMapper guestMapper) {
        this.guestRepository = guestRepository;
        this.guestMapper = guestMapper;
    }

    @Override
    public Guest findOrCreateGuest(CreateBookingRequestDto guestDetails) {
        log.info("Checking whether guest already exists");
        Guest guestEntity = guestMapper.toGuestEntity(guestDetails);

        Optional<Guest> guestOptional = guestRepository.findByPhoneNumber(guestEntity.getPhoneNumber());
        if(guestOptional.isPresent()){
            Guest existingGuest = guestOptional.get();
            log.info("Existing guest found with id={}", existingGuest.getId());
            return existingGuest;
        }

        Guest savedNewGuest = guestRepository.save(guestEntity);
        log.info("New guest created successfully with id={}", savedNewGuest.getId());
        return savedNewGuest;
    }
}
