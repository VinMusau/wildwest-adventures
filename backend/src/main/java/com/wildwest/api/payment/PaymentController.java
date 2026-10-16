package com.wildwest.api.payment;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
public class PaymentController {

    private final PaymentProviderRouter router;
    private final PaystackService paystackService;
    private final BookingRepository bookingRepository;

    public PaymentController(
            PaymentProviderRouter router,
            PaystackService paystackService,
            BookingRepository bookingRepository
    ) {
        this.router = router;
        this.paystackService = paystackService;
        this.bookingRepository = bookingRepository;
    }

    public record PaymentInitResponse(
            String provider,
            String reference,
            String clientKey,
            String accessCode
    ) {
        static PaymentInitResponse from(PaymentProvider.InitResult result) {
            return new PaymentInitResponse(
                    result.provider(),
                    result.reference(),
                    result.clientKey(),
                    result.accessCode()
            );
        }
    }

    @PostMapping("/{reference}/payment-init")
    @Transactional
    public PaymentInitResponse initialize(
            @AuthenticationPrincipal User user,
            @PathVariable String reference
    ) {
        Booking booking = loadOwned(user, reference);
        var result = router.current().initialize(user, booking);
        return PaymentInitResponse.from(result);
    }

    @PostMapping("/{reference}/verify-payment")
    @Transactional
    public Map<String, Object> verifyPayment(
            @AuthenticationPrincipal User user,
            @PathVariable String reference
    ) {
        Booking booking = loadOwned(user, reference);

        if (booking.getPaystackReference() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No payment has been initiated for this booking");
        }

        boolean confirmed = paystackService.verify(booking.getPaystackReference());

        // Reload to reflect any state change
        Booking refreshed = bookingRepository.findByReference(reference).orElseThrow();

        return Map.of(
                "confirmed", confirmed,
                "bookingReference", refreshed.getReference(),
                "status", refreshed.getStatus().name()
        );
    }

    private Booking loadOwned(User user, String reference) {
        Booking booking = bookingRepository.findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        return booking;
    }
}