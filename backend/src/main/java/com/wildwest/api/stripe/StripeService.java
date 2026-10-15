package com.wildwest.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.payment.PaymentProvider;
import com.wildwest.api.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService implements PaymentProvider {

    private final BookingRepository bookingRepository;

    public StripeService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public String name() {
        return "STRIPE";
    }

    @Override
    @Transactional
    public InitResult initialize(User user, Booking booking) {
        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking is not awaiting payment");
        }
        if (booking.getExpiresAt() != null
                && booking.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking hold has expired");
        }

        // Reuse existing intent if still usable
        if (booking.getStripePaymentIntentId() != null) {
            try {
                PaymentIntent existing =
                        PaymentIntent.retrieve(booking.getStripePaymentIntentId());
                if ("requires_payment_method".equals(existing.getStatus())
                        || "requires_confirmation".equals(existing.getStatus())) {
                    return InitResult.stripe(existing.getClientSecret());
                }
            } catch (StripeException ignored) {
                // fall through to create a new one
            }
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("bookingReference", booking.getReference());
        metadata.put("bookingId", booking.getId().toString());

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) booking.getTotalCents())
                    .setCurrency(booking.getCurrency().toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods
                                                    .AllowRedirects.NEVER)
                                    .build()
                    )
                    .putAllMetadata(metadata)
                    .setDescription("Wild West Adventures — " + booking.getTour().getTitle())
                    .setReceiptEmail(booking.getLeadEmail())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            booking.setStripePaymentIntentId(intent.getId());
            booking.setPaymentProvider("STRIPE");
            bookingRepository.save(booking);
            return InitResult.stripe(intent.getClientSecret());
        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Stripe error: " + e.getMessage());
        }
    }
}