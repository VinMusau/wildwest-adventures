package com.wildwest.api.payment;

import com.wildwest.api.booking.BookingPaymentService;
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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/webhooks")
public class PaystackWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaystackWebhookController.class);

    private final BookingRepository bookingRepository;
    private final BookingPaymentService paymentService;
    private final JsonMapper jsonMapper;
    private final String secretKey;

    public PaystackWebhookController(
            BookingRepository bookingRepository,
            BookingPaymentService paymentService,
            JsonMapper jsonMapper,
            @Value("${wildwest.paystack.secret-key:}") String secretKey
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentService = paymentService;
        this.jsonMapper = jsonMapper;
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

        JsonNode root;
        try {
            root = jsonMapper.readTree(payload);
        } catch (JacksonException e) {
            log.warn("Paystack webhook body is not valid JSON");
            return ResponseEntity.status(400).body("Invalid JSON");
        }
        String event = root.path("event").asString(null);
        log.info("Paystack webhook received: {}", event);

        if ("charge.success".equals(event)) {
            JsonNode data = root.path("data");
            String reference = data.path("reference").asString(null);
            String status = data.path("status").asString(null);

            if (reference != null && "success".equals(status)) {
                onChargeSuccess(reference);
            } else {
                log.warn("charge.success missing reference or status: ref={} status={}",
                        reference, status);
            }
        }

        return ResponseEntity.ok("ok");
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
                booking -> paymentService.recordSuccessfulPayment(booking.getId(), "PAYSTACK"),
                () -> log.warn("No booking found for Paystack reference {}", reference)
        );
    }
}