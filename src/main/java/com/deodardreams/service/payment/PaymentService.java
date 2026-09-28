package com.deodardreams.service.payment;

import com.deodardreams.dto.response.PaymentResponseDto;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentResponseDto createPaymentOrder(Long bookingId, BigDecimal amount);

    // Verifies the Razorpay payment signature after checkout.
    void verifyPayment(String razorpayOrderId,
                       String razorpayPaymentId,
                       String razorpaySignature);

    // Marks a payment as failed.
    void markPaymentAsFailed(String razorpayOrderId);

    // Verifies that the webhook request was genuinely sent by Razorpay.
    boolean verifyWebhookSignature(String rawBody, String signature);


}
