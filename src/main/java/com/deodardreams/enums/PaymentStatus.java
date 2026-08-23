package com.deodardreams.enums;

public enum PaymentStatus {
    CREATED,    // Razorpay order created, payment not yet completed
    PAID,       // payment succeeded
    FAILED,     // payment attempt failed
    REFUNDED    // money returned to guest
}
