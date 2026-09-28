package com.deodardreams.dto.response;

import com.deodardreams.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentResponseDto {

    private Long paymentId;

    private String razorpayOrderId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus paymentStatus;
}
