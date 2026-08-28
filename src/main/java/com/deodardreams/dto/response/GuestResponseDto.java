package com.deodardreams.dto.response;

public record GuestResponseDto(Long id,
                               String firstName,
                               String lastName,
                               String city,
                               String state,
                               String email,
                               String phoneNumber) {}
