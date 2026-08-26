package com.deodardreams.service.mail;

import com.deodardreams.dto.response.EnquiryAlertResponseDto;
import com.deodardreams.model.EnquiryUser;

public interface EmailService {
    void sendEnquiryAcknowledgement(
            String recipientEmail,
            String recipientName
    );

    void sendEnquiryAlertToAdmin(EnquiryAlertResponseDto alertResponseDto);
}
