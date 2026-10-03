package com.deodardreams.model;

import com.deodardreams.enums.PaymentAttemptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_attempts")
@Getter
@Setter
public class PaymentAttempt extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // Set when the Razorpay order is first created, before the guest has paid.
    @Column(nullable = false, unique = true)
    private String razorpayOrderId;

    // Only exists AFTER a successful payment — must be nullable, unlike razorpayOrderId.
    private String razorpayPaymentId;

    @Column(nullable = false)
    private BigDecimal expectedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentAttemptStatus status;
}
