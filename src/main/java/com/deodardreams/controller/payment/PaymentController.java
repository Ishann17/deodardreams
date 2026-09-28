package com.deodardreams.controller.payment;

import com.deodardreams.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestHeader(value = "x-razorpay-event-id", required = false) String eventId) {

        boolean signatureValid = paymentService.verifyWebhookSignature(rawBody, signature);
        if(!signatureValid){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }
}
