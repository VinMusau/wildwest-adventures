package com.wildwest.api.availability;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A departure date as the API returns it. Matches the frontend's {@code Availability} type.
 * Build it inside a transaction: {@code priceCents} falls back to the tour's price, which is lazy-loaded.
 */
public record AvailabilityDto(
        UUID id,
        LocalDate date,
        int capacity,
        int booked,
        int remaining,
        int priceCents,
        String currency,
        String status
) {
    public static AvailabilityDto from(Availability a) {
        return new AvailabilityDto(
                a.getId(),
                a.getDepartureDate(),
                a.getCapacity(),
                a.getBooked(),
                a.remaining(),
                a.effectivePriceCents(),
                a.getCurrency(),
                a.getStatus()
        );
    }
}
