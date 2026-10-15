package com.wildwest.api.payment;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.user.User;

public interface PaymentProvider {

    /** Provider identifier: "STRIPE" or "PAYSTACK". */
    String name();

    /**
     * Initialize a payment for the given booking.
     * Returns provider-specific data the frontend needs.
     */
    InitResult initialize(User user, Booking booking);

    record InitResult(String provider, String reference, String clientKey, String accessCode) {
        public static InitResult stripe(String clientSecret) {
            return new InitResult("STRIPE", null, clientSecret, null);
        }
        public static InitResult paystack(String reference, String accessCode) {
            return new InitResult("PAYSTACK", reference, null, accessCode);
        }
    }
}