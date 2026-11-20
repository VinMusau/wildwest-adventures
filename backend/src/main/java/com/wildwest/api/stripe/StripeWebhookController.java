package com.wildwest.api.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.wildwest.api.booking.BookingPaymentService;
import com.wildwest.api.booking.BookingRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeProperties properties;
    private final BookingRepository bookingRepository;
    private final BookingPaymentService paymentService;

    public StripeWebhookController(
            StripeProperties properties, BookingRepository bookingRepository, BookingPaymentService paymentService) {
        this.properties = properties;
        this.bookingRepository = bookingRepository;
        this.paymentService = paymentService;
    }

    @PostMapping("/stripe")
    @Transactional
    public ResponseEntity<String> handle(
            HttpServletRequest request, @RequestHeader("Stripe-Signature") String signature) throws IOException {

        // CRITICAL: read the raw body. Do NOT use @RequestBody.
        String payload = request.getReader().lines().collect(Collectors.joining("\n"));

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, properties.webhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }

        log.info("Stripe webhook received: {} ({})", event.getType(), event.getId());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> onSucceeded(event);
            case "payment_intent.payment_failed" -> onFailed(event);
            default -> log.debug("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("ok");
    }

    private void onSucceeded(Event event) {
        PaymentIntent intent = extractIntent(event);
        if (intent == null) return;

        String method = intent.getPaymentMethod() != null ? intent.getPaymentMethod() : "STRIPE";
        bookingRepository
                .findByStripePaymentIntentId(intent.getId())
                .ifPresentOrElse(
                        booking -> paymentService.recordSuccessfulPayment(booking.getId(), method),
                        () -> log.warn("No booking found for PaymentIntent {}", intent.getId()));
    }

    private void onFailed(Event event) {
        PaymentIntent intent = extractIntent(event);
        if (intent == null) return;

        bookingRepository.findByStripePaymentIntentId(intent.getId()).ifPresent(booking -> {
            // Leave it PENDING so the user can retry. The scheduler will
            // eventually expire it if they never pay.
            log.info(
                    "Payment failed for booking {} (PI {}): {}",
                    booking.getReference(),
                    intent.getId(),
                    intent.getLastPaymentError());
        });
    }

    private PaymentIntent extractIntent(Event event) {
        return event.getDataObjectDeserializer()
                .getObject()
                .filter(PaymentIntent.class::isInstance)
                .map(PaymentIntent.class::cast)
                .orElse(null);
    }
}
