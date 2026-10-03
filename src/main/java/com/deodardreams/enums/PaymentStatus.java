package com.deodardreams.enums;

public enum PaymentStatus {
    PENDING,    // Razorpay order created, payment not yet completed
    PAID,       // payment succeeded
    FAILED,     // payment attempt failed
}
