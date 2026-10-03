package com.deodardreams.exception;

public class RazorpayOrderNotFoundException extends RuntimeException{

    public RazorpayOrderNotFoundException(String message){
        super(message);
    }
}
