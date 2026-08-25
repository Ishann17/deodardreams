package com.deodardreams.service.enquiry;


import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import com.deodardreams.dto.response.EnquiryUserResponseDto;

public interface EnquiryUserService {
    EnquiryUserResponseDto createEnquiry(CreateEnquiryUserRequestDto request);
}
