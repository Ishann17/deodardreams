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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private RoomProductRepository roomProductRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingUnitRepository bookingUnitRepository;
    @Mock
    private GuestService guestService;
    @Mock
    private PhysicalUnitRepository physicalUnitRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private CreateBookingRequestDto requestDto;
    private Guest guest;
    private RoomProduct roomProduct;
    private PhysicalUnit physicalUnit;

    @BeforeEach
    void setUp() {
        requestDto = new CreateBookingRequestDto();
        requestDto.setRoomProductCode(RoomCategory.SINGLE_SUITE.name());
        requestDto.setCheckIn(LocalDate.now().plusDays(1));
        requestDto.setCheckOut(LocalDate.now().plusDays(3));
        requestDto.setNumberOfGuests(1);
        requestDto.setNumberOfRooms(1);

        guest = new Guest();
        guest.setId(1L);
        guest.setFirstName("John");

        roomProduct = new RoomProduct();
        roomProduct.setId(100L);
        roomProduct.setName("Single Suite");
        roomProduct.setMaxOccupancy(2);
        roomProduct.setBasePrice(new BigDecimal("1500.00"));
        roomProduct.setExtraGuestCharge(new BigDecimal("500.00"));

        physicalUnit = new PhysicalUnit();
        physicalUnit.setId(200L);
    }

    @Test
    void createBooking_Success_NormalOccupancy() {
        when(guestService.findOrCreateGuest(requestDto)).thenReturn(guest);
        when(roomProductRepository.findByRoomCategory(RoomCategory.SINGLE_SUITE)).thenReturn(Optional.of(roomProduct));
        when(physicalUnitRepository.findByUnitTypeAndIsActiveTrue(UnitType.INDIVIDUAL_ROOM)).thenReturn(List.of(physicalUnit));
        //when(bookingUnitRepository.findOverlappingBookings(eq(200L), any(LocalDate.class), any(LocalDate.class))).thenReturn(Collections.emptyList());

        Booking savedBooking = new Booking();
        savedBooking.setId(500L);
        savedBooking.setCheckIn(requestDto.getCheckIn());
        savedBooking.setCheckOut(requestDto.getCheckOut());
        savedBooking.setTotalAmount(new BigDecimal("1500.00"));
        savedBooking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDto response = bookingService.createBooking(requestDto);

        assertNotNull(response);
        assertEquals(500L, response.bookingId());
        assertEquals(new BigDecimal("1500.00"), response.totalAmount());
        assertEquals("CONFIRMED", response.status());

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingUnitRepository, times(1)).save(any(BookingUnit.class));
    }

    @Test
    void createBooking_Success_SingleSuite_MaxOccupancy_ExtraCharge() {
        requestDto.setNumberOfGuests(2);

        when(guestService.findOrCreateGuest(requestDto)).thenReturn(guest);
        when(roomProductRepository.findByRoomCategory(RoomCategory.SINGLE_SUITE)).thenReturn(Optional.of(roomProduct));
        when(physicalUnitRepository.findByUnitTypeAndIsActiveTrue(UnitType.INDIVIDUAL_ROOM)).thenReturn(List.of(physicalUnit));
        //when(bookingUnitRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        Booking savedBooking = new Booking();
        savedBooking.setId(501L);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        bookingService.createBooking(requestDto);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        // Verifying basePrice + extraGuestCharge
        assertEquals(new BigDecimal("2000.00"), bookingCaptor.getValue().getTotalAmount());
    }

    @Test
    void createBooking_ThrowsResourceNotFound_WhenRoomProductMissing() {
        when(guestService.findOrCreateGuest(requestDto)).thenReturn(guest);
        when(roomProductRepository.findByRoomCategory(RoomCategory.SINGLE_SUITE)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(requestDto));

        assertTrue(exception.getMessage().contains("is not available"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ThrowsMaxOccupancyExceeded_WhenGuestsTooHigh() {
        requestDto.setNumberOfGuests(5);
        when(guestService.findOrCreateGuest(requestDto)).thenReturn(guest);
        when(roomProductRepository.findByRoomCategory(RoomCategory.SINGLE_SUITE)).thenReturn(Optional.of(roomProduct));

        MaxOccupancyExceededException exception = assertThrows(MaxOccupancyExceededException.class,
                () -> bookingService.createBooking(requestDto));

        assertTrue(exception.getMessage().contains("Maximum occupancy"));
        verify(physicalUnitRepository, never()).findByUnitTypeAndIsActiveTrue(any());
    }

    @Test
    void createBooking_ThrowsRoomNotAvailable_WhenOverlappingBookingsExist() {
        when(guestService.findOrCreateGuest(requestDto)).thenReturn(guest);
        when(roomProductRepository.findByRoomCategory(RoomCategory.SINGLE_SUITE)).thenReturn(Optional.of(roomProduct));
        when(physicalUnitRepository.findByUnitTypeAndIsActiveTrue(UnitType.INDIVIDUAL_ROOM)).thenReturn(List.of(physicalUnit));

        BookingUnit overlappingUnit = new BookingUnit();
        //when(bookingUnitRepository.findOverlappingBookings(eq(200L), any(), any())).thenReturn(List.of(overlappingUnit));

        RoomNotAvailableException exception = assertThrows(RoomNotAvailableException.class,
                () -> bookingService.createBooking(requestDto));

        assertTrue(exception.getMessage().contains("is not available between"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ThrowsIllegalArgumentException_ForThreeBHK() {
        requestDto.setRoomProductCode(RoomCategory.THREE_BHK.name());

        when(guestService.findOrCreateGuest(requestDto)).thenReturn(guest);
        when(roomProductRepository.findByRoomCategory(RoomCategory.THREE_BHK)).thenReturn(Optional.of(roomProduct));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(requestDto));

        assertTrue(exception.getMessage().contains("not configured yet"));
    }
}