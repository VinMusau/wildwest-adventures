package com.wildwest.api.review;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookingId(UUID bookingId);

    List<Review> findByBookingIdIn(Collection<UUID> bookingIds);

    @EntityGraph(attributePaths = {"tour", "booking"})
    List<Review> findByTourSlugAndStatusOrderByCreatedAtDesc(String slug, Review.Status status, Pageable page);

    @EntityGraph(attributePaths = {"tour", "booking", "user"})
    List<Review> findByStatusOrderByCreatedAtDesc(Review.Status status);

    @EntityGraph(attributePaths = {"tour", "booking", "user"})
    List<Review> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"tour", "booking"})
    Optional<Review> findFirstByFeaturedTrueAndStatus(Review.Status status);

    /** Average rating and count of a tour's approved reviews. */
    @Query("""
        select new com.wildwest.api.review.ReviewRepository$Stats(avg(r.rating), count(r))
        from Review r
        where r.tour.id = :tourId and r.status = com.wildwest.api.review.Review$Status.APPROVED
        """)
    Stats statsForTour(@Param("tourId") UUID tourId);

    /** Average rating and count of all approved reviews. */
    @Query("""
        select new com.wildwest.api.review.ReviewRepository$Stats(avg(r.rating), count(r))
        from Review r
        where r.status = com.wildwest.api.review.Review$Status.APPROVED
        """)
    Stats statsOverall();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Review r set r.featured = false where r.featured = true and r.id <> :keep")
    int unfeatureAllExcept(@Param("keep") UUID keep);

    record Stats(Double average, Long count) {}
}
