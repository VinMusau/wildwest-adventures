package com.wildwest.api.admin;

import java.time.Instant;
import java.util.UUID;

public interface CustomerProjection {
    UUID getId();
    String getEmail();
    String getFirstName();
    String getLastName();
    String getRole();
    Instant getCreatedAt();
    long getBookingsCount();
    long getTotalSpentCents();
    Instant getLastBookingAt();
}