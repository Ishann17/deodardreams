package com.deodardreams.exception;

public class RazorpayWebhookSignatureInvalidException extends RuntimeException{

    public RazorpayWebhookSignatureInvalidException(String message){
        super(message);
    }
}
