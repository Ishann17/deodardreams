package com.deodardreams.mapper;

import com.deodardreams.dto.response.PaymentResponseDto;
import com.deodardreams.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentResponseMapper {

    // Payment does not contain currency, so we explicitly set it to INR here.
    @Mapping(target = "currency", constant = "INR")
    PaymentResponseDto toPaymentResponseDto(Payment payment);
}
