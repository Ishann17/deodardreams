package com.deodardreams.service.payment;

import com.deodardreams.dto.response.PaymentResponseDto;
import com.deodardreams.enums.BookingStatus;
import com.deodardreams.enums.PaymentStatus;
import com.deodardreams.enums.RoomCategory;
import com.deodardreams.model.Booking;
import com.deodardreams.model.Guest;
import com.deodardreams.model.RoomProduct;
import com.deodardreams.repository.BookingRepository;
import com.deodardreams.repository.GuestRepository;
import com.deodardreams.repository.RoomProductRepository;
import com.deodardreams.testconfig.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest //this tells "Spring, start my actual application for this test."
@Import(MySqlTestContainerConfig.class) // this tells "Also use the special MySQL Testcontainer configuration for this test."
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService; //passing the actual object to test it rather than creating a mock version

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomProductRepository roomProductRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldCreatePaymentOrder(){

        //We need a guest to create booking
        Guest guest = new Guest();

        guest.setFirstName("Rahul");
        guest.setLastName("Sharma");
        guest.setCity("Delhi");
        guest.setState("Delhi");
        guest.setEmail("rahul.sharma@test.com");
        guest.setPhoneNumber("9000000001");

        // Saves the guest to the test database and updates the entity with its generated ID.
        guest = guestRepository.save(guest);

        //We need room product for booking
        RoomProduct roomProduct = new RoomProduct();

        roomProduct.setRoomCategory(RoomCategory.TWO_BHK);
        roomProduct.setName("2 BHK");
        roomProduct.setBasePrice(BigDecimal.valueOf(4999));
        roomProduct.setExtraGuestCharge(BigDecimal.ZERO);
        roomProduct.setBaseOccupancy(4);
        roomProduct.setMaxOccupancy(6);

        // Saves the room product to the test database and updates the entity with its generated ID.
        roomProduct = roomProductRepository.save(roomProduct);

        Booking booking = new Booking();

        booking.setGuest(guest);
        booking.setRoomProduct(roomProduct);
        booking.setNumberOfGuests(2);
        booking.setNumberOfRooms(1);
        booking.setCheckIn(LocalDate.of(2026, 9, 28));
        booking.setCheckOut(LocalDate.of(2026, 9, 30));
        booking.setTotalAmount(BigDecimal.valueOf(4999));
        // Keeps the booking pending until the guest successfully completes payment.
        booking.setStatus(BookingStatus.PENDING);

        // Saves the booking to the test database and updates the entity with its generated ID.
        booking = bookingRepository.save(booking);

        // Verifies that JPA persisted the booking and generated its database ID.
        assertNotNull(booking.getId());

        PaymentResponseDto paymentResponse = paymentService.createPaymentOrder(booking.getId(), booking.getTotalAmount());
        // Verifies that the payment service returned a response.
        assertNotNull(paymentResponse);

        // Verifies that Razorpay returned an order ID.
        assertNotNull(paymentResponse.getRazorpayOrderId());

        // Verifies that the returned payment amount matches the booking amount.
        assertEquals(booking.getTotalAmount(), paymentResponse.getAmount());

        // Verifies that the payment is initially created and not yet paid.
        assertEquals(PaymentStatus.CREATED, paymentResponse.getPaymentStatus());

        // Razorpay uses INR for this application.
        assertEquals("INR", paymentResponse.getCurrency());
    }


}
