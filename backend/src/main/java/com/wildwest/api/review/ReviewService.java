package com.wildwest.api.review;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.review.ReviewDtos.AdminReview;
import com.wildwest.api.review.ReviewDtos.PublicReview;
import com.wildwest.api.review.ReviewDtos.RatingSummary;
import com.wildwest.api.review.ReviewDtos.SubmitReviewRequest;
import com.wildwest.api.tour.Tour;
import com.wildwest.api.tour.TourRepository;
import com.wildwest.api.user.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Verified reviews. Only the customer who made a confirmed booking can review it, once the
 * trip has ended, and only once. Reviews appear publicly, and count towards the tour's
 * rating, only after an admin approves them.
 */
@Service
public class ReviewService {

    private static final int MAX_PUBLIC_REVIEWS_PER_TOUR = 50;

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;

    public ReviewService(
            ReviewRepository reviewRepository, BookingRepository bookingRepository, TourRepository tourRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
    }

    // ---------- customers ----------

    /** Whether this booking can be reviewed now (ignoring whether a review already exists). */
    public static boolean tripEnded(Booking booking, LocalDate today) {
        return booking.getStatus() == Booking.Status.CONFIRMED
                && !lastDayOf(booking).isAfter(today.minusDays(1));
    }

    private static LocalDate lastDayOf(Booking booking) {
        int days = Math.max(1, booking.getTour().getDurationDays());
        return booking.getDepartureDate().plusDays(days - 1L);
    }

    @Transactional
    public AdminReview submit(User user, String bookingReference, SubmitReviewRequest req) {
        Booking booking = bookingRepository
                .findByReference(bookingReference)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != Booking.Status.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only confirmed bookings can be reviewed");
        }
        if (!tripEnded(booking, LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You can review this trip once it has ended (after " + lastDayOf(booking) + ")");
        }
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You've already reviewed this trip");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setTour(booking.getTour());
        review.setUser(user);
        review.setRating(req.rating());
        review.setTitle(trimOrNull(req.title()));
        review.setBody(req.body().trim());
        review.setAuthorName(displayName(user));
        review.setAuthorCountry(trimOrNull(booking.getLeadCountry()));
        review.setStatus(Review.Status.PENDING);
        return AdminReview.from(reviewRepository.save(review));
    }

    /** "Amina Otieno" becomes "Amina O.". */
    static String displayName(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String name = last.isEmpty() ? first : first + " " + last.charAt(0) + ".";
        return name.isBlank() ? "Guest" : name;
    }

    // ---------- public ----------

    @Transactional(readOnly = true)
    public List<PublicReview> approvedForTour(String slug) {
        return reviewRepository
                .findByTourSlugAndStatusOrderByCreatedAtDesc(
                        slug, Review.Status.APPROVED, PageRequest.of(0, MAX_PUBLIC_REVIEWS_PER_TOUR))
                .stream()
                .map(PublicReview::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PublicReview> featured() {
        return reviewRepository
                .findFirstByFeaturedTrueAndStatus(Review.Status.APPROVED)
                .map(PublicReview::from);
    }

    @Transactional(readOnly = true)
    public RatingSummary summary() {
        return RatingSummary.from(reviewRepository.statsOverall());
    }

    // ---------- admins ----------

    @Transactional(readOnly = true)
    public List<AdminReview> list(Review.Status status) {
        List<Review> reviews = status == null
                ? reviewRepository.findAllByOrderByCreatedAtDesc()
                : reviewRepository.findByStatusOrderByCreatedAtDesc(status);
        return reviews.stream().map(AdminReview::from).toList();
    }

    @Transactional
    public AdminReview approve(UUID id) {
        return moderate(id, Review.Status.APPROVED);
    }

    /** Rejects a pending review, or unpublishes an approved one. */
    @Transactional
    public AdminReview reject(UUID id) {
        return moderate(id, Review.Status.REJECTED);
    }

    /** Features an approved review on the homepage (only one at a time), or unfeatures it. */
    @Transactional
    public AdminReview setFeatured(UUID id, boolean featured) {
        Review review = load(id);
        if (featured && review.getStatus() != Review.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only approved reviews can be featured");
        }
        if (featured) {
            reviewRepository.unfeatureAllExcept(id);
            review = load(id);
        }
        review.setFeatured(featured);
        return AdminReview.from(reviewRepository.save(review));
    }

    private AdminReview moderate(UUID id, Review.Status status) {
        Review review = load(id);
        review.setStatus(status);
        review.setModeratedAt(Instant.now());
        if (status != Review.Status.APPROVED) {
            review.setFeatured(false);
        }
        Review saved = reviewRepository.saveAndFlush(review);
        recomputeTourRating(saved.getTour().getId());
        return AdminReview.from(saved);
    }

    /** tours.rating and tours.review_count cache the tour's approved reviews. */
    private void recomputeTourRating(UUID tourId) {
        RatingSummary summary = RatingSummary.from(reviewRepository.statsForTour(tourId));
        Tour tour = tourRepository.findById(tourId).orElseThrow();
        tour.setRating(summary.averageRating() == null ? BigDecimal.ZERO.setScale(1) : summary.averageRating());
        tour.setReviewCount((int) summary.count());
        tourRepository.save(tour);
    }

    private Review load(UUID id) {
        return reviewRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
