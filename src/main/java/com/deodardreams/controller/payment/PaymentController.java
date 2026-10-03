package com.deodardreams.controller.payment;

import com.deodardreams.dto.response.PaymentCheckoutResponseDto;
import com.deodardreams.dto.response.PaymentResponseDto;
import com.deodardreams.service.payment.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;

    /**
     * Receives webhook events sent by Razorpay.
     *
     * The raw request body is intentionally received as a String because
     * Razorpay signature verification must be performed against the exact
     * payload received from Razorpay.
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = true) String signature,
            @RequestHeader(value = "x-razorpay-event-id", required = true) String eventId) throws JsonProcessingException {

        paymentService.processRazorpayWebhook(rawBody, eventId, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createPaymentOrder(@RequestParam Long bookingId) {
        PaymentCheckoutResponseDto response = paymentService.createPaymentOrder(bookingId);
        return ResponseEntity.ok(response);
    }
}
