package com.wildwest.api.payment;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaystackService implements PaymentProvider {

    private static final String PAYSTACK_BASE = "https://api.paystack.co";

    private final BookingRepository bookingRepository;
    private final RestClient restClient;
    private final String chargeCurrency;
    private final double usdToKesRate;

    public PaystackService(
            BookingRepository bookingRepository,
            @Value("${wildwest.paystack.secret-key:}") String secretKey,
            @Value("${wildwest.paystack.currency:KES}") String chargeCurrency,
            @Value("${wildwest.paystack.usd-to-kes-rate:128.0}") double usdToKesRate
    ) {
        this.bookingRepository = bookingRepository;
        this.chargeCurrency = chargeCurrency.toUpperCase();
        this.usdToKesRate = usdToKesRate;
        this.restClient = RestClient.builder()
                .baseUrl(PAYSTACK_BASE)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String name() {
        return "PAYSTACK";
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

        // Convert the booking total to the currency Paystack will actually charge
        long chargeAmountCents = convertToChargeCurrency(
                booking.getTotalCents(), booking.getCurrency());

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", booking.getLeadEmail());
        payload.put("amount", chargeAmountCents);
        payload.put("currency", chargeCurrency);
        payload.put("reference",
                "WWA-" + booking.getReference() + "-" + System.currentTimeMillis());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("bookingReference", booking.getReference());
        metadata.put("bookingId", booking.getId().toString());
        metadata.put("bookingCurrency", booking.getCurrency());
        metadata.put("bookingAmountCents", String.valueOf(booking.getTotalCents()));
        metadata.put("chargeCurrency", chargeCurrency);
        metadata.put("chargeAmountCents", String.valueOf(chargeAmountCents));
        if (!booking.getCurrency().equalsIgnoreCase(chargeCurrency)) {
            metadata.put("usdToKesRate", String.valueOf(usdToKesRate));
        }
        payload.put("metadata", metadata);

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri("/transaction/initialize")
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Paystack error: " + e.getMessage());
        }

        if (response == null || !Boolean.TRUE.equals(response.get("status"))) {
            String message = response != null
                    ? String.valueOf(response.get("message"))
                    : "Unknown Paystack error";
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        String accessCode = (String) data.get("access_code");
        String reference = (String) data.get("reference");

        booking.setPaystackReference(reference);
        booking.setPaymentProvider("PAYSTACK");
        bookingRepository.save(booking);

        return InitResult.paystack(reference, accessCode);
    }

    /**
     * Convert a booking amount to the currency Paystack will charge in.
     *
     * Paystack's `amount` field is always in the smallest unit of the charge
     * currency (cents for USD, cents for KES — both use 100 minor units per
     * base unit). So converting USD → KES is pure FX: multiply by the rate.
     */
    private long convertToChargeCurrency(int bookingAmountCents, String bookingCurrency) {
        if (bookingCurrency.equalsIgnoreCase(chargeCurrency)) {
            return bookingAmountCents;
        }

        if ("USD".equalsIgnoreCase(bookingCurrency) && "KES".equalsIgnoreCase(chargeCurrency)) {
            double usd = bookingAmountCents / 100.0;
            long kesCents = Math.round(usd * usdToKesRate * 100);
            return kesCents;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Paystack charge currency " + chargeCurrency
                        + " does not support bookings in " + bookingCurrency);
    }
}