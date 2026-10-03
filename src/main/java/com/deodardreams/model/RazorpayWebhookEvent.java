package com.deodardreams.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "razorpay_webhook_events")
@Getter
@Setter
public class RazorpayWebhookEvent extends BaseEntity{

    // Razorpay's unique event ID used to detect and prevent duplicate webhook processing.
    @Column(nullable = false, unique = true)
    private String eventId;

    // Identifies the type of Razorpay event, such as payment.captured or payment.failed.
    @Column(nullable = false)
    private String eventType;

    // Stores when our system successfully processed this webhook event.
    private LocalDateTime processedAt;
}
