package com.deodardreams.service.mail;

public interface EmailService {
    void sendEnquiryAcknowledgement(
            String recipientEmail,
            String recipientName
    );
}
