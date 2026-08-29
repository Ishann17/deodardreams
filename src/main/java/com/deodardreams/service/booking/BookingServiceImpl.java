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
import java.util.Optional;

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

        // Find an existing guest by phone number or create a new guest when this is their first booking.
        Guest guest = guestService.findOrCreateGuest(requestDto);
        log.info("Resolved guest id={} for booking request", guest.getId());

        // Converts the client-provided room product code into the application's RoomCategory enum.
        RoomCategory roomCategory = RoomCategory.valueOf(requestDto.getRoomProductCode());

        // Fetches the configured room product containing the server-side price and occupancy rules.
        RoomProduct roomProduct = roomProductRepository.findByRoomCategory(roomCategory).orElseThrow(() -> new ResourceNotFoundException(
                        "Room Product with category "
                        + roomCategory
                        + " is not available."));

        Integer numberOfGuests = requestDto.getNumberOfGuests();
        Integer numberOfRooms = requestDto.getNumberOfRooms();

        //Determines the maximum number of guests that can be accommodated across all requested rooms.
        int maximumAllowedGuests = roomProduct.getMaxOccupancy() * numberOfRooms;

        if(numberOfGuests > maximumAllowedGuests){
            log.warn("Booking rejected due to occupancy: guests={}, maximumAllowedGuests={}, " + "category={}, rooms={}",
                    numberOfGuests,
                    maximumAllowedGuests,
                    roomCategory,
                    numberOfRooms);

            throw new MaxOccupancyExceededException("Maximum occupancy for " + numberOfRooms + " " + roomProduct.getName() + " room(s) is " + maximumAllowedGuests);
        }

        /*
         * Determine the base price for the complete room request.
         * The price always comes from RoomProduct stored on the server.
         * The client can never control the booking price.
         */
        BigDecimal totalAmount = roomProduct.getBasePrice().multiply(BigDecimal.valueOf(numberOfRooms));

        /*
         * Single Suite allows one extra guest above base occupancy,
         * which requires an additional mattress charge.
         * Guests are distributed across rooms so that each room is filled
         * up to base occupancy before using the extra mattress capacity.
         */

        if(roomCategory == RoomCategory.SINGLE_SUITE){
            List<Integer> guestsPerRoom = distributeGuestsAcrossSingleSuites(numberOfGuests, numberOfRooms, roomProduct.getBaseOccupancy(), roomProduct.getMaxOccupancy());

            int extraGuests = guestsPerRoom.stream().mapToInt(guests -> Math.max(0, guests-roomProduct.getBaseOccupancy())).sum();

            BigDecimal extraCharge = roomProduct.getExtraGuestCharge().multiply(BigDecimal.valueOf(extraGuests));

            totalAmount = totalAmount.add(extraCharge);
            log.info("Single Suite allocation calculated: guestsPerRoom={}, " + "extraGuests={}, extraCharge={}, totalAmount={}",
                    guestsPerRoom,
                    extraGuests,
                    extraCharge,
                    totalAmount);
        }

        log.info(
                "Booking amount calculated: category={}, rooms={}, totalAmount={}",
                roomCategory,
                numberOfRooms,
                totalAmount);

        /*
         * Determine which actual physical units can be allocated for this booking.
         *
         * ONE_BHK uses a special allocation strategy that tries to keep
         * multiple bedrooms inside the same 2 BHK parent unit.
         *
         * SINGLE_SUITE and TWO_BHK use the generic physical-unit availability check.
         */
        List<PhysicalUnit> allocatedPhysicalUnits;

        if (roomCategory == RoomCategory.ONE_BHK) {

            allocatedPhysicalUnits = allocateOneBhkBedrooms(numberOfRooms, requestDto.getCheckIn(), requestDto.getCheckOut());

        } else {

            allocatedPhysicalUnits = getAvailablePhysicalUnits(roomCategory, requestDto.getCheckIn(), requestDto.getCheckOut())
                            .stream()
                            .limit(numberOfRooms)
                            .toList();
        }

        /*
         * The number of available physical units must be at least equal
         * to the number of rooms requested by the guest.
         */
        if (allocatedPhysicalUnits.size() < numberOfRooms) {

            log.warn("Booking rejected due to insufficient availability: category={}, " + "requestedRooms={}, availableRooms={}",
                    roomCategory,
                    numberOfRooms,
                    allocatedPhysicalUnits.size());

            throw new RoomNotAvailableException(
                    "Requested number of "
                            + roomProduct.getName()
                            + " room(s) is not available between "
                            + requestDto.getCheckIn()
                            + " and "
                            + requestDto.getCheckOut()
            );
        }

        log.info("Physical units successfully allocated: category={}, unitIds={}",
                roomCategory, allocatedPhysicalUnits.stream().map(PhysicalUnit::getId).toList());

        /*
         * Creates the booking in PENDING state.
         *
         * The booking will be moved to CONFIRMED only after successful payment.
         */
        Booking booking = new Booking();

        booking.setGuest(guest);
        booking.setRoomProduct(roomProduct);
        booking.setNumberOfGuests(numberOfGuests);
        booking.setNumberOfRooms(numberOfRooms);
        booking.setCheckIn(requestDto.getCheckIn());
        booking.setCheckOut(requestDto.getCheckOut());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        log.info(
                "Booking created successfully with id={}, status={}",
                savedBooking.getId(),
                savedBooking.getStatus()
        );

        /*
         * Creates one BookingUnit for every physical unit allocated to this booking.
         *
         * BookingUnit is the actual inventory reservation used by availability
         * checks to prevent the same physical unit from being double-booked.
         */
        for (PhysicalUnit physicalUnit : allocatedPhysicalUnits) {

            BookingUnit bookingUnit = new BookingUnit();

            bookingUnit.setBooking(savedBooking);
            bookingUnit.setPhysicalUnit(physicalUnit);
            bookingUnit.setCheckIn(savedBooking.getCheckIn());
            bookingUnit.setCheckOut(savedBooking.getCheckOut());

            bookingUnitRepository.save(bookingUnit);

            log.info(
                    "BookingUnit created: bookingId={}, physicalUnitId={}",
                    savedBooking.getId(),
                    physicalUnit.getId()
            );
        }

        log.info(
                "Booking flow completed successfully for bookingId={}",
                savedBooking.getId()
        );
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

    @Override
    public BookingResponseDto cancelBooking(Long bookingId) {
        log.info("Cancelling booking with id={}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id " + bookingId + " does not exist"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.info("Booking with id={} is already cancelled", bookingId);
            return new BookingResponseDto(
                    booking.getId(),
                    booking.getGuest().getFirstName(),
                    booking.getRoomProduct().getName(),
                    booking.getCheckIn(),
                    booking.getCheckOut(),
                    booking.getTotalAmount(),
                    booking.getStatus().toString()
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking with id={} cancelled successfully", bookingId);

        return new BookingResponseDto(
                updatedBooking.getId(),
                updatedBooking.getGuest().getFirstName(),
                updatedBooking.getRoomProduct().getName(),
                updatedBooking.getCheckIn(),
                updatedBooking.getCheckOut(),
                updatedBooking.getTotalAmount(),
                updatedBooking.getStatus().toString()
        );
    }

    @Override
    public List<BookingResponseDto> getAllBookings() {
        log.info("Fetching all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        log.info("Fetched {} booking(s)", bookings.size());
        return bookings.stream()
                .map(b -> new BookingResponseDto(
                        b.getId(),
                        b.getGuest().getFirstName(),
                        b.getRoomProduct().getName(),
                        b.getCheckIn(),
                        b.getCheckOut(),
                        b.getTotalAmount(),
                        b.getStatus().toString()
                ))
                .toList();
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
      return physicalUnits.stream().filter(physicalUnit -> bookingUnitRepository.findOverlappingBookings(physicalUnit.getId(), checkIn, checkOut, BookingStatus.CANCELLED).isEmpty()).toList();
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
                                        checkOut,
                                        BookingStatus.CANCELLED
                                ).isEmpty()).toList();
    }

    private List<PhysicalUnit> allocateOneBhkBedrooms(
            Integer numberOfRooms,
            LocalDate checkIn,
            LocalDate checkOut) {

        List<PhysicalUnit> twoBhkUnits = getActiveTwoBhkUnits();

        // Phase 1: prefer a single 2BHK unit that alone has enough free bedrooms —
        // this is what keeps 1BHK guests consolidated and leaves other units untouched.
        for (PhysicalUnit twoBhkUnit : twoBhkUnits) {
            List<PhysicalUnit> availableBedrooms = getAvailableOneBhkBedrooms(twoBhkUnit.getId(), checkIn, checkOut);
            if (availableBedrooms.size() >= numberOfRooms) {
                log.info("1BHK allocation: fully satisfied within single 2BHK unitId={}", twoBhkUnit.getId());
                return availableBedrooms.subList(0, numberOfRooms);
            }
        }

        // Phase 2: no single unit has enough — pool free bedrooms across all 2BHK units instead
        // of rejecting the booking, since enough total capacity may still exist.
        log.info("1BHK allocation: no single 2BHK unit has {} free bedroom(s), pooling across units", numberOfRooms);
        List<PhysicalUnit> pooledBedrooms = twoBhkUnits.stream()
                .flatMap(unit -> getAvailableOneBhkBedrooms(unit.getId(), checkIn, checkOut).stream())
                .toList();

        if (pooledBedrooms.size() >= numberOfRooms) {
            log.info("1BHK allocation: satisfied via pooling, {} bedroom(s) found", pooledBedrooms.size());
            return pooledBedrooms.subList(0, numberOfRooms);
        }

        return List.of();
    }
}
