package com.wildwest.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.wildwest.api.admin.CustomerProjection;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = """
    SELECT u.id                AS id,
           u.email             AS email,
           u.first_name        AS first_name,
           u.last_name         AS last_name,
           u.role              AS role,
           u.created_at        AS created_at,
           COUNT(b.id)         AS bookings_count,
           COALESCE(SUM(CASE WHEN b.paid_at IS NOT NULL
                              THEN b.total_cents - COALESCE(b.refund_amount_cents, 0)
                              ELSE 0 END), 0) AS total_spent_cents,
           MAX(b.created_at)   AS last_booking_at
    FROM users u
    LEFT JOIN bookings b ON b.user_id = u.id
    GROUP BY u.id
    ORDER BY u.created_at DESC
    """,
            countQuery = "SELECT COUNT(*) FROM users",
            nativeQuery = true)
    Page<CustomerProjection> findAllWithBookingStats(Pageable pageable);
}