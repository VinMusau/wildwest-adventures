package com.wildwest.api.payment;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/bookings")
public class PaymentController {

    private final PaymentProviderRouter router;
    private final BookingRepository bookingRepository;

    public PaymentController(
            PaymentProviderRouter router,
            BookingRepository bookingRepository
    ) {
        this.router = router;
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
        Booking booking = bookingRepository.findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }

        var result = router.current().initialize(user, booking);
        return PaymentInitResponse.from(result);
    }
}