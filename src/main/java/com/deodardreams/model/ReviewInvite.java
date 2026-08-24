package com.deodardreams.model;

/**
 * A one-time, unguessable link sent to a guest after checkout, inviting them
 * to leave a review. Separate from Review itself — this table controls ACCESS
 * (can this link still be used), Review controls CONTENT (what they said).
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_invites")
@Getter
@Setter
public class ReviewInvite extends BaseEntity {

    // Which booking this invite is for — always required, only sent for real website stays.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Random, unguessable string sent in the email/SMS link — this IS the security mechanism.
    @Column(nullable = false, unique = true)
    private String token;

    // Marked true once a review is submitted through this link, so it can't be reused.
    @Column(nullable = false)
    private boolean used = false;

    // Link stops working after this — prevents stale links from being usable indefinitely.
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
