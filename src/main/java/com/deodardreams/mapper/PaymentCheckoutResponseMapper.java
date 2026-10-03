package com.deodardreams.mapper;

import com.deodardreams.dto.response.PaymentCheckoutResponseDto;
import com.deodardreams.model.Payment;
import com.deodardreams.model.PaymentAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCheckoutResponseMapper {

    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "paymentAttemptId", source = "paymentAttempt.id")
    @Mapping(target = "razorpayOrderId", source = "paymentAttempt.razorpayOrderId")
    @Mapping(target = "amount", source = "payment.amount")
    @Mapping(target = "currency", constant = "INR")
    @Mapping(target = "paymentAttemptStatus", source = "paymentAttempt.status")
    PaymentCheckoutResponseDto toPaymentCheckoutResponse(Payment payment, PaymentAttempt paymentAttempt);
}
