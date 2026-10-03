package com.deodardreams.dto.response;

import com.deodardreams.enums.PaymentAttemptStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentCheckoutResponseDto {

    // Identifies the overall payment record associated with the booking.
    private Long paymentId;

    // Identifies the specific payment attempt currently being used.
    private Long paymentAttemptId;

    // Identifies the Razorpay order that the frontend must use for Checkout.
    private String razorpayOrderId;

    // Represents the exact amount that the customer needs to pay.
    private BigDecimal amount;

    // Indicates the currency in which the payment must be made.
    private String currency;

    // Represents the current state of this specific payment attempt.
    private PaymentAttemptStatus paymentAttemptStatus;
}
