package com.wildwest.api.booking;

import java.util.UUID;

/** Booking status changes that other parts of the app (e.g. email) react to. */
public final class BookingEvents {

    private BookingEvents() {}

    /** The booking is now CONFIRMED (paid, or confirmed by an admin). */
    public record Confirmed(UUID bookingId) {}

    /** The booking was cancelled by an admin. */
    public record Cancelled(UUID bookingId) {}
}
