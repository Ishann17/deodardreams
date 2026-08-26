package com.deodardreams.service.enquiry;


import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import com.deodardreams.dto.response.EnquiryAlertResponseDto;
import com.deodardreams.dto.response.EnquiryUserResponseDto;
import com.deodardreams.model.EnquiryUser;

public interface EnquiryUserService {
    EnquiryUserResponseDto createEnquiry(CreateEnquiryUserRequestDto request);
    void createEnquiryAlertToAdmin(EnquiryUser enquiryUser);
}
