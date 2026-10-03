package com.deodardreams.service.payment;

import com.deodardreams.dto.response.PaymentCheckoutResponseDto;
import com.deodardreams.dto.response.PaymentResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentCheckoutResponseDto createPaymentOrder(Long bookingId);

    // Marks a payment attempt as failed.
    void markPaymentAttemptAsFailed(String razorpayOrderId);

    // Verifies that the webhook request was genuinely sent by Razorpay.
    boolean verifyWebhookSignature(String rawBody, String signature);

    // Processes a signature-verified Razorpay webhook event and updates the corresponding payment state.
    void processRazorpayWebhook(String rawBody, String eventId, String signature);

}
