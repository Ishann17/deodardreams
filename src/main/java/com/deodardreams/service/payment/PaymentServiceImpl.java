package com.deodardreams.service.payment;

import com.deodardreams.dto.response.PaymentCheckoutResponseDto;
import com.deodardreams.enums.BookingStatus;
import com.deodardreams.enums.PaymentAttemptStatus;
import com.deodardreams.enums.PaymentStatus;
import com.deodardreams.exception.*;
import com.deodardreams.mapper.PaymentCheckoutResponseMapper;
import com.deodardreams.mapper.PaymentResponseMapper;
import com.deodardreams.model.Booking;
import com.deodardreams.model.Payment;
import com.deodardreams.model.PaymentAttempt;
import com.deodardreams.model.RazorpayWebhookEvent;
import com.deodardreams.repository.BookingRepository;
import com.deodardreams.repository.PaymentAttemptRepository;
import com.deodardreams.repository.PaymentRepository;
import com.deodardreams.repository.RazorpayWebhookEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService{

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentResponseMapper paymentResponseMapper;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final RazorpayWebhookEventRepository razorpayWebhookEventRepository;
    private final PaymentCheckoutResponseMapper paymentCheckoutResponseMapper;
    private final ObjectMapper objectMapper;

    // RazorpayClient is used to communicate with Razorpay's API and create/manage payment orders.
    private final RazorpayClient razorpayClient;


    @Override
    public PaymentCheckoutResponseDto createPaymentOrder(Long bookingId) {
        log.info("Creating payment order for bookingId: {}", bookingId);

        //Check if Booking exists
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking with id " + bookingId + " is not available"));
        log.info("Booking found. bookingId: {}, totalAmount: {}", bookingId, booking.getTotalAmount());
        // Razorpay requires the amount in the smallest currency unit (paise for INR).
        long amountInPaise = convertToPaise(booking.getTotalAmount());

        // Holds the Razorpay order details in JSON format required by the Razorpay API.
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "booking_" + bookingId);

        //Check if payment exists for above booking
        Optional<Payment> paymentExists = paymentRepository.findByBookingId(booking.getId());

        Payment payment;
        if(paymentExists.isEmpty()){
            log.info("No existing payment found. Creating payment record for bookingId: {}", bookingId);
            //Since Payment for the above booking is does not exists we create one.
            payment = new Payment();
            payment.setBooking(booking);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setAmount(booking.getTotalAmount());
            // Saves the payment record with PENDING status until the guest completes payment.
            payment = paymentRepository.save(payment);
            log.info("Payment created. paymentId: {}, bookingId: {}, status: {}",
                    payment.getId(), bookingId, payment.getPaymentStatus());
        }
        else{
            payment = paymentExists.get();
            log.debug("Existing payment found. paymentId: {}, bookingId: {}, status: {}",
                    payment.getId(), bookingId, payment.getPaymentStatus());
        }

        if(payment.getPaymentStatus() == PaymentStatus.PAID){
            log.warn("Payment already completed. Rejecting new payment attempt for bookingId: {}, paymentId: {}",
                    bookingId, payment.getId());
            throw new PaymentAlreadyCompletedException("Payment for booking " + bookingId + " is already completed");
        }

        Optional<PaymentAttempt> attemptedPayment = paymentAttemptRepository.findByPayment_IdAndStatus(payment.getId(), PaymentAttemptStatus.CREATED);
        log.debug("Checked for existing CREATED payment attempt. paymentId: {}, attemptPresent: {}",
                payment.getId(), attemptedPayment.isPresent());

        if(attemptedPayment.isPresent()){
            log.info("Reusing existing payment attempt. paymentId: {}, paymentAttemptId: {}, razorpayOrderId: {}",
                    payment.getId(),
                    attemptedPayment.get().getId(),
                    attemptedPayment.get().getRazorpayOrderId());
            return paymentCheckoutResponseMapper.toPaymentCheckoutResponse(payment, attemptedPayment.get());
        }

        if(payment.getPaymentStatus() == PaymentStatus.PENDING){
            try {
                // Creates the order on Razorpay and returns the generated Razorpay order details.
                log.info("Creating Razorpay order. bookingId: {}, paymentId: {}, amount: {}",
                        bookingId, payment.getId(), payment.getAmount());
                Order razorpayOrder = razorpayClient.orders.create(orderRequest);

                // Extracts the unique Razorpay order ID returned after successfully creating the order.
                String razorpayOrderId = razorpayOrder.get("id");
                log.info("Razorpay order created successfully. bookingId: {}, paymentId: {}, razorpayOrderId: {}",
                        bookingId, payment.getId(), razorpayOrderId);
                PaymentAttempt paymentAttempt = new PaymentAttempt();
                paymentAttempt.setRazorpayOrderId(razorpayOrderId);
                paymentAttempt.setPayment(payment);
                paymentAttempt.setExpectedAmount(payment.getAmount());
                paymentAttempt.setStatus(PaymentAttemptStatus.CREATED);

                PaymentAttempt savedPaymentAttempt = paymentAttemptRepository.save(paymentAttempt);
                log.info("Payment attempt created. paymentId: {}, paymentAttemptId: {}, razorpayOrderId: {}, status: {}",
                        payment.getId(),
                        savedPaymentAttempt.getId(),
                        savedPaymentAttempt.getRazorpayOrderId(),
                        savedPaymentAttempt.getStatus());
                return paymentCheckoutResponseMapper.toPaymentCheckoutResponse(payment, savedPaymentAttempt);

            } catch (RazorpayException e) {
                log.error("Failed to create Razorpay order. bookingId: {}, paymentId: {}",
                        bookingId, payment.getId(), e);
                throw new PaymentException("Failed to create Razorpay order", e);
            }
        }
        return null;
    }


    @Override
    public void markPaymentAttemptAsFailed(String razorpayOrderId) {

    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        try{
            return Utils.verifyWebhookSignature(rawBody, signature, webhookSecret);
        }catch (RazorpayException e){
            throw new PaymentException("Failed to verify Razorpay webhook signature", e);
        }
    }

    @Transactional
    @Override
    public void processRazorpayWebhook(String rawBody, String eventId, String signature){
        log.info("Received Razorpay webhook event. eventId: {}", eventId);

        boolean signatureValid = verifyWebhookSignature(rawBody, signature);
        if(!signatureValid){
            log.warn("Invalid Razorpay webhook signature. eventId: {}", eventId);
            throw new RazorpayWebhookSignatureInvalidException("Invalid Razorpay webhook signature");
        }
        log.info("Razorpay webhook signature verified successfully. eventId: {}", eventId);

        Optional<RazorpayWebhookEvent> existingEvent = razorpayWebhookEventRepository.findByEventId(eventId);

        // Checks whether this Razorpay webhook event was already processed successfully.
        // A non-null processedAt means the event has already completed processing, so we safely ignore the duplicate delivery.
        if(existingEvent.isPresent() && existingEvent.get().getProcessedAt() != null){
            log.info("Duplicate Razorpay webhook detected. eventId: {}, eventType: {}, processedAt: {}. Skipping processing.",
                    eventId, existingEvent.get().getEventType(), existingEvent.get().getProcessedAt());
            return;
        }

        try{
            //Reading raw body from Razorpay webhook event
            JsonNode webhookJson = objectMapper.readTree(rawBody);

            log.debug("Razorpay webhook payload parsed successfully. eventId: {}", eventId);

            // Extracts the Razorpay event type, such as payment.captured or payment.failed, so the webhook can be routed to the appropriate processing logic.
            String eventType = webhookJson.get("event").asText();
            log.info("Razorpay webhook event type received. eventId: {}, eventType: {}", eventId, eventType);

            // Creates a database record for this webhook event before processing it.
            // The unique eventId acts as an idempotency key to prevent duplicate processing.
            RazorpayWebhookEvent webhookEvent = new RazorpayWebhookEvent();
            webhookEvent.setEventId(eventId);
            webhookEvent.setEventType(eventType);
            // saveAndFlush() -> forces Hibernate to execute the INSERT immediately
            //Flushes the webhook event INSERT to the database immediately so the UNIQUE eventId constraint can detect concurrent duplicate webhook deliveries before we continue processing the payment.
            razorpayWebhookEventRepository.saveAndFlush(webhookEvent);
            log.info("Razorpay webhook event registered for processing. eventId: {}, eventType: {}", eventId, eventType);

            switch (eventType){

                case "payment.captured":
                    log.info("Processing Razorpay payment.captured event. eventId: {}", eventId);

                    // Navigating the correct Razorpay JSON structure
                    JsonNode paymentEntity = webhookJson.get("payload").get("payment").get("entity");

                    String webHookPaymentId = paymentEntity.get("id").asText();
                    String webHookOrderId = paymentEntity.get("order_id").asText();

                    log.info("Received payment.captured event. PaymentID: {}, OrderID: {}", webHookPaymentId, webHookOrderId);
                    Optional<PaymentAttempt> paymentAttemptByRazorpayOrderId = paymentAttemptRepository.findByRazorpayOrderId(webHookOrderId);
                    if(paymentAttemptByRazorpayOrderId.isEmpty()){
                        throw new RazorpayOrderNotFoundException("Razorpay order " + webHookOrderId + " is not associated with any payment attempt");
                    }
                    PaymentAttempt paymentAttempt = paymentAttemptByRazorpayOrderId.get();

                    // Fetches the payment directly from Razorpay so we can independently verify
                    // that the payment belongs to the expected Razorpay order before updating our database.
                    com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(webHookPaymentId);

                    // Extracts the Razorpay order ID from the fetched payment details so we can
                    // verify that this payment actually belongs to the order we created.
                    String razorpayPaymentOrderId = razorpayPayment.get("order_id");

                    // Rejects the webhook if the payment belongs to a different Razorpay order,
                    // preventing us from updating the wrong PaymentAttempt.
                    if (!webHookOrderId.equals(razorpayPaymentOrderId)) {
                        log.error("Razorpay payment/order mismatch. eventId: {}, webhookOrderId: {}, razorpayPaymentOrderId: {}",
                                eventId, webHookOrderId, razorpayPaymentOrderId);
                        throw new RazorpayPaymentOrderMismatchException("Razorpay payment does not belong to the expected order");
                    }

                    // Extracts the amount reported by Razorpay in paise so it can be compared
                    // safely with the expected payment amount from our PaymentAttempt.
                    long razorpayAmountInPaise = ((Number) razorpayPayment.get("amount")).longValue();
                    long expectedAmountInPaise = convertToPaise(paymentAttempt.getExpectedAmount());

                    log.debug("Verifying payment amount. paymentAttemptId: {}, expectedAmountInPaise: {}, razorpayAmountInPaise: {}",
                            paymentAttempt.getId(), expectedAmountInPaise, razorpayAmountInPaise);

                    if(razorpayAmountInPaise != expectedAmountInPaise){
                        log.error("Razorpay payment amount mismatch. paymentAttemptId: {}, expectedAmountInPaise: {}, razorpayAmountInPaise: {}",
                                paymentAttempt.getId(), expectedAmountInPaise, razorpayAmountInPaise);
                        throw new PaymentAmountMismatchException("Payment amount mismatch. Expected amount: " +
                                expectedAmountInPaise + " paise, but Razorpay reported: " +
                                razorpayAmountInPaise + " paise");
                    }

                    // Extracts the currency reported by Razorpay so we can verify it matches
                    // the currency in which our PaymentAttempt was created.
                    String razorpayCurrency = razorpayPayment.get("currency");

                    log.debug("Verifying payment currency. paymentAttemptId: {}, expectedCurrency: INR, razorpayCurrency: {}", paymentAttempt.getId(),razorpayCurrency);

                    if (!"INR".equalsIgnoreCase(razorpayCurrency)) {
                        log.error("Razorpay payment currency mismatch. paymentAttemptId: {}, expectedCurrency: INR, razorpayCurrency: {}", paymentAttempt.getId(), razorpayCurrency);
                        throw new RazorpayCurrencyMismatchException("Payment currency mismatch. Expected INR, but Razorpay reported: " + razorpayCurrency);
                    }

                    // Extracts the payment status reported by Razorpay so we can verify that
                    // the payment is actually captured before updating our database.
                    String razorpayPaymentStatus = razorpayPayment.get("status");

                    log.debug("Verifying Razorpay payment status. paymentAttemptId: {}, razorpayPaymentStatus: {}", paymentAttempt.getId(), razorpayPaymentStatus);

                    if (!"captured".equalsIgnoreCase(razorpayPaymentStatus)) {
                        log.error("Razorpay payment is not captured. paymentAttemptId: {}, razorpayPaymentStatus: {}", paymentAttempt.getId(), razorpayPaymentStatus);
                        throw new RazorpayPaymentNotCapturedException("Razorpay payment is not captured. Payment status: " + razorpayPaymentStatus);
                    }

                    // Marks this specific Razorpay payment attempt as successfully paid and stores the Razorpay payment ID for future reconciliation and audit history.
                    paymentAttempt.setStatus(PaymentAttemptStatus.PAID);
                    paymentAttempt.setRazorpayPaymentId(webHookPaymentId);
                    PaymentAttempt paidPaymentAttempt = paymentAttemptRepository.save(paymentAttempt);
                    log.info("Payment attempt marked as PAID. paymentAttemptId: {}, razorpayOrderId: {}, razorpayPaymentId: {}, status: {}",
                            paymentAttempt.getId(), paymentAttempt.getRazorpayOrderId(), paymentAttempt.getRazorpayPaymentId(), paymentAttempt.getStatus());

                    // Updates the overall payment for the booking to PAID because a valid payment attempt has completed successfully.
                    Payment paymentRecord = paidPaymentAttempt.getPayment();
                    paymentRecord.setPaymentStatus(PaymentStatus.PAID);
                    Payment updatedPayment = paymentRepository.save(paymentRecord);
                    log.info("Payment marked as PAID. paymentId: {}, bookingId: {}, status: {}",  paymentRecord.getId(),
                            paymentRecord.getBooking().getId(), paymentRecord.getPaymentStatus());

                    // Confirms the booking only after the payment has been successfully verified and recorded as PAID.
                    Booking pendingBooking = updatedPayment.getBooking();
                    pendingBooking.setStatus(BookingStatus.CONFIRMED);
                    Booking confirmedBooking = bookingRepository.save(pendingBooking);
                    log.info("Booking confirmed after successful payment. bookingId: {}, paymentId: {}, status: {}",
                            confirmedBooking.getId(), updatedPayment.getId(), confirmedBooking.getStatus());

                    webhookEvent.setProcessedAt(LocalDateTime.now());
                    razorpayWebhookEventRepository.save(webhookEvent);
                    log.info("Razorpay webhook event processed successfully. eventId: {}, eventType: {}", eventId, eventType);

                    break;

                case "payment.failed":
                    log.info("Processing Razorpay payment.failed event. eventId: {}", eventId);
                    break;

                default:
                    log.warn("Unsupported Razorpay webhook event. eventId: {}, eventType: {}", eventId, eventType);
                    break;
            }

        }catch (JsonProcessingException e){
            log.error("Failed to parse Razorpay webhook payload for eventId: {}", eventId, e);
            throw new PaymentException("Failed to parse Razorpay webhook payload", e);
        } catch (RazorpayException e) {
            throw new PaymentException("Failed to fetch Razorpay payment details", e);
        }
    }

    // Converts the booking amount from rupees to paise, as required by Razorpay.
    private long convertToPaise(BigDecimal amount) {
        // longValueExact() prevents silent loss of precision. If the BigDecimal cannot be represented exactly as a long, Java throws an exception instead of silently giving you an incorrect amount.
        return amount.multiply(BigDecimal.valueOf(100)).longValueExact();
    }
}
