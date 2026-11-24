package com.wildwest.api.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.booking.BookingDtos.LeadInput;
import com.wildwest.api.booking.BookingDtos.TravelerInput;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.booking.BookingService;
import com.wildwest.api.review.ReviewDtos.AdminReview;
import com.wildwest.api.review.ReviewDtos.SubmitReviewRequest;
import com.wildwest.api.tour.Tour;
import com.wildwest.api.tour.TourRepository;
import com.wildwest.api.user.User;
import com.wildwest.api.user.UserRepository;
import com.wildwest.api.user.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class ReviewServiceTests {

    private static final String TOUR = "kenya-family-safari";
    private static final String GOOD_BODY = "Wonderful guides and the camps were spotless. Would go again.";

    @Autowired
    ReviewService reviewService;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @Autowired
    TourRepository tourRepository;

    @Autowired
    UserRepository userRepository;

    private final List<User> users = new ArrayList<>();
    private Availability departure;
    private int bookedBefore;
    private BigDecimal ratingBefore;
    private int reviewCountBefore;

    @BeforeEach
    void setUp() {
        List<Availability> dates = availabilityRepository.findByTourSlugOrderByDepartureDateAsc(TOUR);
        departure = dates.get(dates.size() - 1);
        bookedBefore = departure.getBooked();
        Tour tour = tourRepository.findBySlug(TOUR).orElseThrow();
        ratingBefore = tour.getRating();
        reviewCountBefore = tour.getReviewCount();
    }

    @AfterEach
    void tearDown() {
        for (User u : users) {
            List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(u.getId());
            reviewRepository.deleteAll(reviewRepository.findByBookingIdIn(
                    bookings.stream().map(Booking::getId).toList()));
            bookingRepository.deleteAll(bookings);
            userRepository.delete(u);
        }
        Availability a = availabilityRepository.findById(departure.getId()).orElseThrow();
        a.setBooked(bookedBefore);
        availabilityRepository.save(a);
        Tour tour = tourRepository.findBySlug(TOUR).orElseThrow();
        tour.setRating(ratingBefore);
        tour.setReviewCount(reviewCountBefore);
        tourRepository.save(tour);
    }

    @Test
    void onlyTheCustomerCanReviewAConfirmedTripThatHasEnded() {
        User amina = user("Amina", "Otieno");

        Booking upcoming = confirmedBooking(amina, false);
        assertThatThrownBy(() -> submit(amina, upcoming, 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("once it has ended");

        Booking unpaid = booking(amina);
        travelInThePast(unpaid);
        assertThatThrownBy(() -> submit(amina, unpaid, 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only confirmed bookings");

        Booking travelled = confirmedBooking(amina, true);
        User stranger = user("Sam", "Stranger");
        assertThatThrownBy(() -> submit(stranger, travelled, 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Booking not found");

        AdminReview review = submit(amina, travelled, 5);
        assertThat(review.status()).isEqualTo("PENDING");
        assertThat(review.authorName()).isEqualTo("Amina O.");

        assertThatThrownBy(() -> submit(amina, travelled, 4))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already reviewed");
    }

    @Test
    void pendingReviewsAreNotPublicAndDontCount() {
        User amina = user("Amina", "Otieno");
        submit(amina, confirmedBooking(amina, true), 5);

        assertThat(reviewService.approvedForTour(TOUR))
                .noneMatch(r -> r.authorName().equals("Amina O."));
        assertThat(tour().getReviewCount()).isEqualTo(reviewCountBefore);
    }

    @Test
    void approvingAndRejectingRecomputesTheTourRating() {
        User amina = user("Amina", "Otieno");
        User juma = user("Juma", "Kariuki");
        AdminReview five = submit(amina, confirmedBooking(amina, true), 5);
        AdminReview four = submit(juma, confirmedBooking(juma, true), 4);

        reviewService.approve(five.id());
        reviewService.approve(four.id());
        assertThat(tour().getReviewCount()).isEqualTo(reviewCountBefore + 2);
        assertThat(reviewService.approvedForTour(TOUR))
                .extracting(r -> r.authorName())
                .contains("Amina O.", "Juma K.");
        if (reviewCountBefore == 0) {
            assertThat(tour().getRating()).isEqualByComparingTo("4.5");
        }

        reviewService.reject(four.id()); // unpublish
        assertThat(tour().getReviewCount()).isEqualTo(reviewCountBefore + 1);
        assertThat(reviewService.approvedForTour(TOUR))
                .noneMatch(r -> r.authorName().equals("Juma K."));
        if (reviewCountBefore == 0) {
            assertThat(tour().getRating()).isEqualByComparingTo("5.0");
        }
    }

    @Test
    void onlyOneApprovedReviewIsFeatured() {
        User amina = user("Amina", "Otieno");
        User juma = user("Juma", "Kariuki");
        AdminReview first = submit(amina, confirmedBooking(amina, true), 5);
        AdminReview second = submit(juma, confirmedBooking(juma, true), 5);

        assertThatThrownBy(() -> reviewService.setFeatured(first.id(), true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only approved");

        reviewService.approve(first.id());
        reviewService.approve(second.id());
        reviewService.setFeatured(first.id(), true);
        reviewService.setFeatured(second.id(), true);

        assertThat(reviewService.featured()).get().extracting(r -> r.id()).isEqualTo(second.id());
        assertThat(reviewRepository.findById(first.id()).orElseThrow().isFeatured())
                .isFalse();

        reviewService.reject(second.id()); // unpublishing also unfeatures
        assertThat(reviewService.featured()).isEmpty();
    }

    @Test
    void myBookingsSayWhetherATripCanBeReviewed() {
        User amina = user("Amina", "Otieno");
        Booking travelled = confirmedBooking(amina, true);
        Booking upcoming = confirmedBooking(amina, false);

        assertThat(mine(amina, travelled).canReview()).isTrue();
        assertThat(mine(amina, upcoming).canReview()).isFalse();

        submit(amina, travelled, 5);
        BookingDto after = mine(amina, travelled);
        assertThat(after.canReview()).isFalse();
        assertThat(after.reviewStatus()).isEqualTo("PENDING");
    }

    // ---------- helpers ----------

    private User user(String first, String last) {
        User u = new User();
        u.setEmail("review-test-" + UUID.randomUUID() + "@example.test");
        u.setPasswordHash("not-used");
        u.setFirstName(first);
        u.setLastName(last);
        u.setRole(UserRole.USER);
        User saved = userRepository.save(u);
        users.add(saved);
        return saved;
    }

    private Booking booking(User user) {
        BookingDto dto = bookingService.create(
                user,
                new CreateBookingRequest(
                        TOUR,
                        departure.getDepartureDate(),
                        List.of(new TravelerInput("Ann", "Lee", "ADULT")),
                        new LeadInput("Ann", "Lee", "ann@example.com", null, "Kenya"),
                        null));
        return bookingRepository.findByReference(dto.reference()).orElseThrow();
    }

    /** A paid booking, either for a trip that has ended or one still to come. */
    private Booking confirmedBooking(User user, boolean travelled) {
        Booking b = booking(user);
        b.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(b);
        if (travelled) travelInThePast(b);
        return bookingRepository.findByReference(b.getReference()).orElseThrow();
    }

    private void travelInThePast(Booking b) {
        Booking fresh = bookingRepository.findByReference(b.getReference()).orElseThrow();
        fresh.setDepartureDate(LocalDate.now().minusDays(60));
        bookingRepository.save(fresh);
    }

    private AdminReview submit(User user, Booking booking, int rating) {
        return reviewService.submit(
                user, booking.getReference(), new SubmitReviewRequest(rating, "Great trip", GOOD_BODY));
    }

    private BookingDto mine(User user, Booking booking) {
        return bookingService.listMine(user).stream()
                .filter(b -> b.reference().equals(booking.getReference()))
                .findFirst()
                .orElseThrow();
    }

    private Tour tour() {
        return tourRepository.findBySlug(TOUR).orElseThrow();
    }
}
