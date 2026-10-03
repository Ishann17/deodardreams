package com.deodardreams.repository;

import com.deodardreams.enums.PaymentAttemptStatus;
import com.deodardreams.model.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    //Added this method because the webhook will contain the Razorpay order ID, and our backend needs to connect that event to our own PaymentAttempt
    Optional<PaymentAttempt> findByRazorpayOrderId(String razorpayOrderId);
    Optional<PaymentAttempt> findByPayment_IdAndStatus(Long paymentId, PaymentAttemptStatus status);
}
