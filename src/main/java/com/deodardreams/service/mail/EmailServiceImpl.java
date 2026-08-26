package com.deodardreams.service.mail;

import com.deodardreams.dto.response.EnquiryAlertResponseDto;
import io.micrometer.core.annotation.Timed;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;
    private static final String ENQUIRY_ACKNOWLEDGEMENT_TEMPLATE = "mail/enquiry-acknowledgement.html";

    @Value("${deodar.dreams.contact-phone}")
    private String contactPhone;
    @Value("${deodar.dreams.alert-mail}")
    private String contactMail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /*@Timed("enquiry.email")
    @Override
    @Async
    public void sendEnquiryAcknowledgement(String recipientEmail, String recipientName) {

        log.info("Starting enquiry acknowledgement email");

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipientEmail);
        message.setSubject("Thank you for contacting Deodar Dreams");

        message.setText("""
                Dear %s,

                Thank you for reaching out to Deodar Dreams, your finest staycation destination in Dehradun.

                We have received your enquiry and our team will get back to you shortly.

                For any immediate assistance, please contact us at %s.

                Regards,
                Deodar Dreams Management
                """.formatted(recipientName, contactPhone));

        mailSender.send(message);
        log.info("Enquiry acknowledgement email sent successfully");
    }*/

    // Measures how long enquiry acknowledgement email delivery takes.
    @Timed("enquiry.email")
    @Async
    @Override
    public void sendEnquiryAcknowledgement(String recipientEmail, String recipientName) {

        log.info("Starting enquiry acknowledgement email");

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(recipientEmail);
            helper.setSubject("Thank you for contacting Deodar Dreams");

            String htmlBody = loadEnquiryAcknowledgementTemplate(recipientName, contactPhone);

            helper.setText(htmlBody, true);

            helper.addInline(
                    "deodar-dreams-hero",
                    new ClassPathResource("mail/deodar-dreams-hero.PNG")
            );

            mailSender.send(message);

            log.info("Enquiry acknowledgement email sent successfully");

        } catch (MessagingException exception) {
            log.error("Failed to send enquiry acknowledgement email", exception);
        } catch (IOException exception) {
            log.error("Failed to load enquiry acknowledgement email template", exception);
        }
    }

    @Override
    @Async
    public void sendEnquiryAlertToAdmin(EnquiryAlertResponseDto alertResponseDto) {
        log.info("Sending enquiry alert to admin for enquiryId={}", alertResponseDto.getId());
        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(contactMail);
            mailMessage.setSubject("ENQUIRY ALERT! #" + alertResponseDto.getId());
            mailMessage.setText(buildEnquiryAlertBody(alertResponseDto));

            mailSender.send(mailMessage);
            log.info("Enquiry alert email sent successfully for enquiryId={}", alertResponseDto.getId());
        } catch (Exception e) {
            log.error("Failed to send enquiry alert email for enquiryId={}", alertResponseDto.getId(), e);
        }
    }
    // Builds the plain-text body from whatever fields are actually present.
    // Optional fields (lastName, dates, message) are skipped entirely when empty,
    // rather than showing up as blank/awkward lines in the email.
    private String buildEnquiryAlertBody(EnquiryAlertResponseDto alertResponseDto) {
        StringBuilder mailBody = new StringBuilder();
        mailBody.append("New Enquiry Received.\n\n");
        mailBody.append("Name : ").append(alertResponseDto.getFirstName());

        if(alertResponseDto.getLastName() != null && !alertResponseDto.getLastName().trim().isEmpty()){
            mailBody.append(" ").append(alertResponseDto.getLastName());
        }
        mailBody.append("\n");
        mailBody.append("Email: ").append(alertResponseDto.getEmail()).append("\n");
        mailBody.append("Mobile: ").append(alertResponseDto.getMobile()).append("\n");

        if( alertResponseDto.getNumberOfRooms() != null && alertResponseDto.getNumberOfRooms() > 0){
            mailBody.append("Number of Rooms: ").append(alertResponseDto.getNumberOfRooms()).append("\n");
        }

        if(alertResponseDto.getNumberOfAdults() != null && alertResponseDto.getNumberOfAdults() > 0){
            mailBody.append("Number of Adults: ").append(alertResponseDto.getNumberOfAdults()).append("\n");
        }

        if(alertResponseDto.getChildrenBelow12() != null && alertResponseDto.getChildrenBelow12() > 0){
            mailBody.append("Number of Children: ").append(alertResponseDto.getChildrenBelow12()).append("\n");
        }

        if (alertResponseDto.getCheckIn() != null) {
            mailBody.append("Check-in: ").append(alertResponseDto.getCheckIn()).append("\n");
        }
        if (alertResponseDto.getCheckOut() != null) {
            mailBody.append("Check-out: ").append(alertResponseDto.getCheckOut()).append("\n");
        }
        if (alertResponseDto.getEnquiryMessage() != null && !alertResponseDto.getEnquiryMessage().trim().isEmpty()) {
            mailBody.append("Message: ").append(alertResponseDto.getEnquiryMessage()).append("\n");
        }
        log.info("Enquiry alert email mail body created for enquiryId={}", alertResponseDto.getId());
        return mailBody.toString();
    }

    // Reads the HTML template off the classpath and substitutes the ${...}
    // placeholders it contains. Kept as a private helper (rather than a
    // separate class) since, right now, it's the only template this service
    // renders — pull it into its own component only once a second template
    // shows up and the duplication actually justifies it.
    private String loadEnquiryAcknowledgementTemplate(String recipientName, String contactPhone) throws IOException {

        String template = StreamUtils.copyToString(
                new ClassPathResource(ENQUIRY_ACKNOWLEDGEMENT_TEMPLATE).getInputStream(),
                StandardCharsets.UTF_8
        );

        return template
                .replace("${recipientName}", recipientName)
                .replace("${contactPhone}", contactPhone);
    }
}