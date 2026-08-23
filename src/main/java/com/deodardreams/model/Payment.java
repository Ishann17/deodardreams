package com.deodardreams.model;
/**
 * The Razorpay payment record tied to one booking. One booking has at most
 * one payment (OneToOne) — created when the Razorpay order is generated,
 * updated once the guest actually pays.
 */

import com.deodardreams.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    // OneToOne, not ManyToOne — a booking should have at most one payment record, not many.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Set when the Razorpay order is first created, before the guest has paid.
    @Column(nullable = false)
    private String razorpayOrderId;

    // Only exists AFTER a successful payment — must be nullable, unlike razorpayOrderId.
    private String razorpayPaymentId;

    // What's owed. BigDecimal — same reasoning as Booking.totalAmount and RoomProduct.basePrice.
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

}