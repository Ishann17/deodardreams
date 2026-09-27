package com.deodardreams.dto.response;

import com.deodardreams.enums.PaymentStatus;

import java.math.BigDecimal;

public class PaymentResponseDto {

    private Long paymentId;

    private String razorpayOrderId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus paymentStatus;
}
