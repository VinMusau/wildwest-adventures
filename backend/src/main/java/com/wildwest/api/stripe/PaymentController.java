package com.wildwest.api.stripe;

import com.wildwest.api.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{reference}/payment-intent")
    public PaymentService.PaymentIntentDto createIntent(
            @AuthenticationPrincipal User user,
            @PathVariable String reference
    ) {
        return paymentService.createIntent(user, reference);
    }
}