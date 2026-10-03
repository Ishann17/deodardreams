package com.deodardreams.repository;

import com.deodardreams.model.PaymentAttempt;
import com.deodardreams.model.RazorpayWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RazorpayWebhookEventRepository extends JpaRepository<RazorpayWebhookEvent, Long> {
    Optional<RazorpayWebhookEvent> findByEventId(String eventId);
}
