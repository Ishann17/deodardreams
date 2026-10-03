package com.deodardreams.exception;

public class RazorpayPaymentNotCapturedException extends RuntimeException {

    public RazorpayPaymentNotCapturedException(String message) {
        super(message);
    }
}
