package com.wildwest.api.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    boolean existsByReference(String reference);

    @EntityGraph(attributePaths = {"tour", "tour.destination", "availability"})
    Optional<Booking> findByReference(String reference);

    @EntityGraph(attributePaths = {"tour", "tour.destination"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);
}