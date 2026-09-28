package com.deodardreams.service.payment;

import com.deodardreams.dto.response.PaymentResponseDto;
import com.deodardreams.enums.PaymentStatus;
import com.deodardreams.exception.PaymentException;
import com.deodardreams.exception.ResourceNotFoundException;
import com.deodardreams.mapper.PaymentResponseMapper;
import com.deodardreams.model.Booking;
import com.deodardreams.model.Payment;
import com.deodardreams.repository.BookingRepository;
import com.deodardreams.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentResponseMapper paymentResponseMapper;
    // RazorpayClient is used to communicate with Razorpay's API and create/manage payment orders.
    private final RazorpayClient razorpayClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository, PaymentResponseMapper paymentResponseMapper, RazorpayClient razorpayClient) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.paymentResponseMapper = paymentResponseMapper;
        this.razorpayClient = razorpayClient;
    }

    @Override
    public PaymentResponseDto createPaymentOrder(Long bookingId, BigDecimal amount) {

        // Razorpay requires the amount in the smallest currency unit (paise for INR).
        long amountInPaise = convertToPaise(amount);

        // Holds the Razorpay order details in JSON format required by the Razorpay API.
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "booking_" + bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking with id " + bookingId + " is not available"
                        ));

        try {
            // Creates the order on Razorpay and returns the generated Razorpay order details.
            Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            // Extracts the unique Razorpay order ID returned after successfully creating the order.
            String razorpayOrderId = razorpayOrder.get("id");

            Payment payment = new Payment();
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setBooking(booking);
            payment.setPaymentStatus(PaymentStatus.CREATED);
            payment.setAmount(amount);

            // Saves the payment record with CREATED status until the guest completes payment.
            Payment savedPayment = paymentRepository.save(payment);

            return paymentResponseMapper.toPaymentResponseDto(savedPayment);

        } catch (RazorpayException e) {
            throw new PaymentException("Failed to create Razorpay order", e);
        }
    }

    @Override
    public void verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {

    }

    @Override
    public void markPaymentAsFailed(String razorpayOrderId) {

    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signature) {

        try{
            return Utils.verifyWebhookSignature(rawBody, signature, webhookSecret);
        }catch (RazorpayException e){
            throw new PaymentException("Failed to verify Razorpay webhook signature", e);
        }
    }

    // Converts the booking amount from rupees to paise, as required by Razorpay.
    private long convertToPaise(BigDecimal amount) {
        // longValueExact() prevents silent loss of precision. If the BigDecimal cannot be represented exactly as a long, Java throws an exception instead of silently giving you an incorrect amount.
        return amount.multiply(BigDecimal.valueOf(100)).longValueExact();
    }
}
