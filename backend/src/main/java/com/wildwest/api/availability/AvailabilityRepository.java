package com.wildwest.api.availability;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    @Query("""
        select a from Availability a
        where a.tour.slug = :slug
          and a.departureDate between :from and :to
          and a.status = 'OPEN'
        order by a.departureDate asc
        """)
    List<Availability> findOpenForTourBetween(
            @Param("slug") String slug,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a from Availability a
        where a.tour.slug = :slug
          and a.departureDate = :date
        """)
    Optional<Availability> lockForUpdate(
            @Param("slug") String slug,
            @Param("date") LocalDate date
    );

    List<Availability> findByTourSlugOrderByDepartureDateAsc(String slug);
}