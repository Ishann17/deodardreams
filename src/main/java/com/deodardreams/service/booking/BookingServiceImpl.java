package com.deodardreams.service.booking;

import com.deodardreams.dto.request.CreateBookingRequestDto;
import com.deodardreams.dto.response.BookingResponseDto;
import com.deodardreams.enums.BookingStatus;
import com.deodardreams.enums.RoomCategory;
import com.deodardreams.enums.UnitType;
import com.deodardreams.exception.MaxOccupancyExceededException;
import com.deodardreams.exception.ResourceNotFoundException;
import com.deodardreams.exception.RoomNotAvailableException;
import com.deodardreams.model.*;
import com.deodardreams.repository.BookingRepository;
import com.deodardreams.repository.BookingUnitRepository;
import com.deodardreams.repository.PhysicalUnitRepository;
import com.deodardreams.repository.RoomProductRepository;
import com.deodardreams.service.guest.GuestService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService{

    private final RoomProductRepository roomProductRepository;
    private final BookingRepository bookingRepository;
    private final BookingUnitRepository bookingUnitRepository;
    private final GuestService guestService;
    private final PhysicalUnitRepository physicalUnitRepository;

    public BookingServiceImpl(RoomProductRepository roomProductRepository, BookingRepository bookingRepository, BookingUnitRepository bookingUnitRepository, GuestService guestService, PhysicalUnitRepository physicalUnitRepository) {
        this.roomProductRepository = roomProductRepository;
        this.bookingRepository = bookingRepository;
        this.bookingUnitRepository = bookingUnitRepository;
        this.guestService = guestService;
        this.physicalUnitRepository = physicalUnitRepository;
    }


    @Override
    public BookingResponseDto createBooking(CreateBookingRequestDto requestDto) {

        log.info("Booking request received: roomProductCode={}, checkIn={}, checkOut={}, numberOfGuests={}",
                requestDto.getRoomProductCode(), requestDto.getCheckIn(), requestDto.getCheckOut(), requestDto.getNumberOfGuests());

        //Find Or Create Guest
        Guest guest = guestService.findOrCreateGuest(requestDto);
        log.info("Resolved guest id={} for booking request", guest.getId());

        // Converts the requested product code into the enum used to identify the sellable room category.
        RoomCategory roomCategory = RoomCategory.valueOf(requestDto.getRoomProductCode());
        // Fetches the configured room product to obtain its pricing and occupancy rules.
        RoomProduct roomProduct = roomProductRepository.findByRoomCategory(roomCategory).orElseThrow(() -> new ResourceNotFoundException("Room Product with category " + roomCategory + " is not available."));

        // Determines whether the requested number of guests exceeds the room's maximum occupancy.
        Integer numberOfGuests = requestDto.getNumberOfGuests();

        if(numberOfGuests > roomProduct.getMaxOccupancy()){
            throw new MaxOccupancyExceededException("Maximum occupancy for " + roomProduct.getName() + " is " + roomProduct.getMaxOccupancy());
        }


        // Determine the base price for the booking.
        // Uses the room product's server-side price instead of accepting any price from the client.
        BigDecimal basePrice = roomProduct.getBasePrice();

        // Single Suite allows one extra guest with an additional mattress charge of ₹500.
        if(roomCategory == RoomCategory.SINGLE_SUITE && numberOfGuests.equals(roomProduct.getMaxOccupancy())){
            basePrice = basePrice.add(roomProduct.getExtraGuestCharge());
        }

        // Calculates the final booking amount including any applicable extra guest charge.
        BigDecimal totalAmount = basePrice;
        log.info("Calculated totalAmount={} for productId={}, numberOfGuests={}", totalAmount, roomProduct.getId(), numberOfGuests);

        Booking booking = new Booking();

        booking.setGuest(guest);
        booking.setRoomProduct(roomProduct);
        booking.setNumberOfGuests(numberOfGuests);
        booking.setNumberOfRooms(requestDto.getNumberOfRooms());
        booking.setCheckIn(requestDto.getCheckIn());
        booking.setCheckOut(requestDto.getCheckOut());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);

        List<PhysicalUnit> availablePhysicalUnits = getAvailablePhysicalUnits(roomCategory, requestDto.getCheckIn(), requestDto.getCheckOut());
        log.info("Found {} available physical unit(s) for category={}, checkIn={}, checkOut={}",
                availablePhysicalUnits.size(), roomCategory, requestDto.getCheckIn(), requestDto.getCheckOut());
        if (availablePhysicalUnits.isEmpty()) {
            log.warn("Booking rejected: no available units for category={} between {} and {}",
                    roomCategory, requestDto.getCheckIn(), requestDto.getCheckOut());
            throw new RoomNotAvailableException("This room is not available between " + requestDto.getCheckIn() + " and " + requestDto.getCheckOut());
        }
        PhysicalUnit physicalUnit = availablePhysicalUnits.getFirst();
        log.info("Allocating physicalUnitId={} for this booking", physicalUnit.getId());
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking saved with id={}, status={}", savedBooking.getId(), savedBooking.getStatus());

        BookingUnit bookingUnit = new BookingUnit();
        bookingUnit.setBooking(savedBooking);
        bookingUnit.setPhysicalUnit(physicalUnit);
        bookingUnit.setCheckIn(savedBooking.getCheckIn());
        bookingUnit.setCheckOut(savedBooking.getCheckOut());

        bookingUnitRepository.save(bookingUnit);
        log.info("BookingUnit saved: bookingId={}, physicalUnitId={}", savedBooking.getId(), physicalUnit.getId());

        log.info("Booking flow completed successfully for bookingId={}", savedBooking.getId());

        return new BookingResponseDto(
                savedBooking.getId(),
                guest.getFirstName(),
                roomProduct.getName(),
                savedBooking.getCheckIn(),
                savedBooking.getCheckOut(),
                savedBooking.getTotalAmount(),
                savedBooking.getStatus().toString()
        );
    }

    private List<Integer> distributeGuestsAcrossSingleSuites(Integer numberOfGuests, Integer numberOfRooms, Integer baseOccupancy, Integer maxOccupancy){

        // Stores how many guests will be assigned to each Single Suite.
        List<Integer> guestsPerRoom = new ArrayList<>();

        // Tracks guests who are still waiting to be assigned to a room.
        int remainingGuests = numberOfGuests;

        // First fill each room up to its base occupancy.
        for(int i=0; i<numberOfRooms && remainingGuests>0; i++){

            int guestForRoom = Math.min(remainingGuests, baseOccupancy);
            guestsPerRoom.add(guestForRoom);
            remainingGuests -= guestForRoom;

        }

        // If guests are still unassigned, use the extra capacity of 1 guest per room.
        // This represents the ₹500 extra-mattress option for a Single Suite.
        for (int i = 0; i < guestsPerRoom.size() && remainingGuests > 0; i++) {

            int currentGuests = guestsPerRoom.get(i);
            int availableExtraCapacity = maxOccupancy - currentGuests;

            int extraGuests = Math.min(remainingGuests, availableExtraCapacity);

            guestsPerRoom.set(i, currentGuests + extraGuests);
            remainingGuests -= extraGuests;
        }


        return guestsPerRoom;
    }

    private UnitType getRequiredUnitType(RoomCategory roomCategory){
        return switch (roomCategory){
            case SINGLE_SUITE -> UnitType.INDIVIDUAL_ROOM;
            case ONE_BHK -> UnitType.ONE_BHK_BEDROOM;
            case TWO_BHK -> UnitType.TWO_BHK_UNIT;
            case THREE_BHK -> throw new IllegalArgumentException(
                    "Physical unit mapping for THREE_BHK is not configured yet"
            );
        };
    }

    private List<PhysicalUnit> getAvailablePhysicalUnits(RoomCategory roomCategory, LocalDate checkIn, LocalDate checkOut){

        UnitType requiredUnitType = getRequiredUnitType(roomCategory);
        // Step 1: get every active unit of the right type — e.g. all bedrooms, if this is a 1 BHK request
        List<PhysicalUnit> physicalUnits = physicalUnitRepository.findByUnitTypeAndIsActiveTrue(requiredUnitType);

        // Step 2: from those, keep only the ones with NO overlapping BookingUnit rows for these dates.
      return physicalUnits.stream().filter(physicalUnit -> bookingUnitRepository.findOverlappingBookings(physicalUnit.getId(), checkIn, checkOut).isEmpty()).toList();
    }

    private List<PhysicalUnit> getActiveTwoBhkUnits() {
        // Finds active 2 BHK parent units that can potentially provide bedrooms for 1 BHK bookings.
        List<PhysicalUnit> twoBhkUnits = physicalUnitRepository.findByUnitTypeAndIsActiveTrue(UnitType.TWO_BHK_UNIT);
        log.info("Found {} active 2 BHK unit(s) for 1 BHK allocation", twoBhkUnits.size());
        return twoBhkUnits;
    }

    private List<PhysicalUnit> getAvailableOneBhkBedrooms(Long parentUnitId, LocalDate checkIn, LocalDate checkOut){
        // Finds all active bedrooms belonging to this specific 2 BHK parent unit.
        List<PhysicalUnit> bedrooms =
                physicalUnitRepository.findByParentUnitIdAndUnitTypeAndIsActiveTrue(parentUnitId, UnitType.ONE_BHK_BEDROOM);
        // Keeps only bedrooms that have no booking overlapping the requested dates.
        return bedrooms.stream().filter(bedroom ->
                        bookingUnitRepository
                                .findOverlappingBookings(
                                        bedroom.getId(),
                                        checkIn,
                                        checkOut
                                ).isEmpty()).toList();
    }

    private List<PhysicalUnit> allocateOneBhkBedrooms(
            Integer numberOfRooms,
            LocalDate checkIn,
            LocalDate checkOut) {

        List<PhysicalUnit> twoBhkUnits = getActiveTwoBhkUnits();

        for (PhysicalUnit twoBhkUnit : twoBhkUnits) {

            List<PhysicalUnit> availableBedrooms =
                    getAvailableOneBhkBedrooms(
                            twoBhkUnit.getId(),
                            checkIn,
                            checkOut
                    );

            if (availableBedrooms.size() >= numberOfRooms) {
                // Returns enough bedrooms from the same 2 BHK parent.
                return availableBedrooms.subList(0, numberOfRooms);
            }
        }

        return List.of();
    }
}
