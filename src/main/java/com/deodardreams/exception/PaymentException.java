package com.deodardreams.exception;

/**
 * Custom exception used when a payment-related operation fails.
 *
 * This exception allows us to convert low-level payment provider exceptions,
 * such as {@code RazorpayException}, into an application-specific exception
 * that is meaningful to our application.
 *
 * The {@code cause} parameter stores the original exception that caused
 * this failure. Keeping the original cause is important because it preserves
 * the actual technical error and its stack trace for debugging and logging.
 *
 * For example:
 *
 * catch (RazorpayException e) {
 *     throw new PaymentException("Failed to create Razorpay order", e);
 * }
 *
 *
 * Here, {@code e} is the original Razorpay exception and becomes the
 * {@code cause} of {@code PaymentException}. This gives us a meaningful
 * application-level error message while still preserving the original
 * exception for troubleshooting.
 */

public class PaymentException  extends RuntimeException{
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
