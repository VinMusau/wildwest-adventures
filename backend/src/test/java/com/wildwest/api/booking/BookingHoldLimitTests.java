package com.wildwest.api.booking;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.booking.BookingDtos.LeadInput;
import com.wildwest.api.booking.BookingDtos.TravelerInput;
import com.wildwest.api.user.User;
import com.wildwest.api.user.UserRepository;
import com.wildwest.api.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BookingHoldLimitTests {

    private static final String TOUR = "seychelles-island-hopping";

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired AvailabilityRepository availabilityRepository;
    @Autowired UserRepository userRepository;

    private User user;
    private Availability departure;
    private int bookedBefore;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("hold-limit-test-" + UUID.randomUUID() + "@example.test");
        user.setPasswordHash("not-used");
        user.setFirstName("Hold");
        user.setLastName("Limit");
        user.setRole(UserRole.USER);
        user = userRepository.save(user);
        List<Availability> dates = availabilityRepository.findByTourSlugOrderByDepartureDateAsc(TOUR);
        departure = dates.get(dates.size() - 1);
        bookedBefore = departure.getBooked();
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll(bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        userRepository.delete(user);
        Availability a = availabilityRepository.findById(departure.getId()).orElseThrow();
        a.setBooked(bookedBefore);
        availabilityRepository.save(a);
    }

    @Test
    void customerCanHoldAtMostThreeUnpaidBookings() {
        book(); book(); book();

        assertThatThrownBy(this::book)
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already have 3 unpaid reservations");
    }

    @Test
    void lapsedAndPaidBookingsDontCount() {
        BookingDto first = book();
        BookingDto second = book();
        book();

        Booking lapsed = bookingRepository.findByReference(first.reference()).orElseThrow();
        lapsed.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        bookingRepository.save(lapsed);
        Booking paid = bookingRepository.findByReference(second.reference()).orElseThrow();
        paid.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(paid);

        assertThat(book().status()).isEqualTo("PENDING");
    }

    private BookingDto book() {
        return bookingService.create(user, new CreateBookingRequest(TOUR, departure.getDepartureDate(),
                List.of(new TravelerInput("Ann", "Lee", "ADULT")),
                new LeadInput("Ann", "Lee", "ann@example.com", null, null), null));
    }
}
