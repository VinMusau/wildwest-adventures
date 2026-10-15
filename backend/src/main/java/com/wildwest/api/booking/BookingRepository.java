package com.wildwest.api.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    boolean existsByReference(String reference);

    @EntityGraph(attributePaths = {"tour", "tour.destination", "availability"})
    Optional<Booking> findByReference(String reference);

    @EntityGraph(attributePaths = {"tour", "tour.destination"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("""
        update Booking b
        set b.status = 'EXPIRED',
            b.updatedAt = current_timestamp
        where b.status = 'PENDING'
        and b.expiresAt is not null
        and b.expiresAt < :now
        """)
    int expirePendingOlderThan(@Param("now") Instant now);

    @Query("""
        select b from Booking b
        where b.status = 'PENDING'
        and b.expiresAt is not null
        and b.expiresAt < :now
        """)
    List<Booking> findExpiredPending(@Param("now") Instant now);

    Optional<Booking> findByStripePaymentIntentId(String paymentIntentId);
}