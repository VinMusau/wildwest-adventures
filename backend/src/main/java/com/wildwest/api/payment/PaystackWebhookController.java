package com.wildwest.api.payment;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/webhooks")
public class PaystackWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaystackWebhookController.class);

    private static final Pattern EVENT_PATTERN =
            Pattern.compile("\"event\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern REFERENCE_PATTERN =
            Pattern.compile("\"reference\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern STATUS_PATTERN =
            Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"");

    private final BookingRepository bookingRepository;
    private final String secretKey;

    public PaystackWebhookController(
            BookingRepository bookingRepository,
            @Value("${wildwest.paystack.secret-key:}") String secretKey
    ) {
        this.bookingRepository = bookingRepository;
        this.secretKey = secretKey;
    }

    @PostMapping("/paystack")
    @Transactional
    public ResponseEntity<String> handle(
            HttpServletRequest request,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature
    ) throws IOException {

        String payload = request.getReader().lines().collect(Collectors.joining("\n"));

        if (signature == null || !verifySignature(payload, signature)) {
            log.warn("Paystack webhook signature verification failed");
            return ResponseEntity.status(400).body("Invalid signature");
        }

        String event = firstMatch(EVENT_PATTERN, payload);
        log.info("Paystack webhook received: {}", event);

        if ("charge.success".equals(event)) {
            String reference = firstMatch(REFERENCE_PATTERN, payload);
            String status = firstMatch(STATUS_PATTERN, payload);

            if (reference != null && "success".equals(status)) {
                onChargeSuccess(reference);
            } else {
                log.warn("charge.success missing reference or status: ref={} status={}",
                        reference, status);
            }
        }

        return ResponseEntity.ok("ok");
    }

    private static String firstMatch(Pattern pattern, String input) {
        Matcher m = pattern.matcher(input);
        return m.find() ? m.group(1) : null;
    }

    private boolean verifySignature(String payload, String receivedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec key = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(key);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    receivedSignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }

    private void onChargeSuccess(String reference) {
        bookingRepository.findByPaystackReference(reference).ifPresentOrElse(
                booking -> {
                    if (booking.getStatus() == Booking.Status.CONFIRMED) {
                        log.info("Booking {} already confirmed, ignoring duplicate",
                                booking.getReference());
                        return;
                    }
                    booking.setStatus(Booking.Status.CONFIRMED);
                    booking.setPaidAt(Instant.now());
                    booking.setPaymentMethod("PAYSTACK");
                    bookingRepository.save(booking);
                    log.info("Booking {} confirmed via Paystack", booking.getReference());
                },
                () -> log.warn("No booking found for Paystack reference {}", reference)
        );
    }
}