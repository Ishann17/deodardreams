package com.deodardreams.model;

/**
 * A guest's rating + written feedback for a completed stay. Always tied to a
 * real Booking — only website guests can leave reviews. Requires admin
 * approval (status) before it's shown publicly.
 */

import com.deodardreams.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review extends BaseEntity {

    // Every review traces back to a real booking — no anonymous/off-platform reviews in-app.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private int rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String reviewText;

    // Admin must approve before this shows publicly — see ReviewStatus.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;
}