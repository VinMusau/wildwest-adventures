package com.wildwest.api.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    boolean existsByReference(String reference);

    /** Unpaid bookings whose seat hold is still running. */
    long countByUserIdAndStatusAndExpiresAtAfter(UUID userId, Booking.Status status, Instant now);

    @EntityGraph(attributePaths = {"tour", "tour.destination", "availability"})
    Optional<Booking> findByReference(String reference);

    @EntityGraph(attributePaths = {"tour", "tour.destination"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Pending bookings whose seat hold has run out. */
    @Query("""
        select b.id from Booking b
        where b.status = 'PENDING'
        and b.expiresAt is not null
        and b.expiresAt < :now
        """)
    List<UUID> findExpiredPendingIds(@Param("now") Instant now);

    /**
     * Marks one booking EXPIRED, but only if it is still PENDING with a lapsed hold.
     * Returns 1 if it changed. The status check makes this safe against a payment
     * confirming the booking at the same moment: only one of the two can win.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update Booking b
        set b.status = 'EXPIRED',
            b.updatedAt = current_timestamp
        where b.id = :id
        and b.status = 'PENDING'
        and b.expiresAt is not null
        and b.expiresAt < :now
        """)
    int expireIfPending(@Param("id") UUID id, @Param("now") Instant now);

    Optional<Booking> findByStripePaymentIntentId(String paymentIntentId);
    
    Optional<Booking> findByPaystackReference(String paystackReference);

    /**
     * Confirms a booking only if it is still in the given status. Returns 1 if it changed.
     * Conditional so that a payment and the hold-expiry job can't both win.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update Booking b
        set b.status = 'CONFIRMED',
            b.paidAt = :paidAt,
            b.paymentMethod = :method,
            b.expiresAt = null,
            b.updatedAt = current_timestamp
        where b.id = :id
        and b.status = :expected
        """)
    int confirmIfStatus(
            @Param("id") UUID id,
            @Param("expected") Booking.Status expected,
            @Param("paidAt") Instant paidAt,
            @Param("method") String method
    );

    @Query(value = """
    SELECT COUNT(*) FROM bookings
    WHERE created_at >= :from AND created_at < :to
    """, nativeQuery = true)
    long countCreatedBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
    SELECT COALESCE(SUM(total_cents - COALESCE(refund_amount_cents, 0)), 0)
    FROM bookings
    WHERE paid_at >= :from AND paid_at < :to
    """, nativeQuery = true)
    long sumRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
    SELECT CAST(date_trunc('day', paid_at) AS date) AS bucket,
           COALESCE(SUM(total_cents - COALESCE(refund_amount_cents, 0)), 0) AS revenue_cents,
           COUNT(*) AS bookings_count
    FROM bookings
    WHERE paid_at IS NOT NULL
      AND paid_at >= :from
    GROUP BY 1
    ORDER BY 1
    """, nativeQuery = true)
    List<RevenuePointProjection> revenueByDay(@Param("from") Instant from);

    @Query(value = """
    SELECT t.slug                AS tour_slug,
           t.title               AS tour_title,
           COUNT(b.id)           AS bookings_count,
           COALESCE(SUM(CASE WHEN b.paid_at IS NOT NULL
                              THEN b.total_cents - COALESCE(b.refund_amount_cents, 0)
                              ELSE 0 END), 0) AS revenue_cents
    FROM tours t
    LEFT JOIN bookings b ON b.tour_id = t.id
    GROUP BY t.id, t.slug, t.title
    ORDER BY bookings_count DESC, revenue_cents DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<TopTourProjection> findTopTours(@Param("limit") int limit);

    long countByStatus(Booking.Status status);

    List<Booking> findTop10ByOrderByCreatedAtDesc();

    interface RevenuePointProjection {

        java.time.LocalDate getBucket();

        long getRevenueCents();

        long getBookingsCount();
    }

    interface TopTourProjection {

        String getTourSlug();

        String getTourTitle();

        long getBookingsCount();

        long getRevenueCents();
    }
}