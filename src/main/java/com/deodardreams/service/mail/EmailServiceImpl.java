package com.deodardreams.service.mail;

import io.micrometer.core.annotation.Timed;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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