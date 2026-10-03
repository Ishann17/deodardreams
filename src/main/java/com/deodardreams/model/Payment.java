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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    // OneToOne, not ManyToOne — a booking should have at most one payment record, not many.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // What's owed. BigDecimal — same reasoning as Booking.totalAmount and RoomProduct.basePrice.
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    /**
     * Stores all payment attempts associated with this payment.
     *
     * A single booking/payment can have multiple payment attempts because a user
     * may fail a payment and try again. Keeping these attempts as separate records
     * allows us to preserve the complete payment history instead of overwriting
     * previous attempts.
     *
     * Payment owns the relationship conceptually, while PaymentAttempt stores the
     * actual foreign key through its "payment" field.
     *
     * LAZY loading is used so payment attempts are fetched from the database only
     * when they are actually needed.
     */

    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY)
    private List<PaymentAttempt> paymentAttempts = new ArrayList<>();

}