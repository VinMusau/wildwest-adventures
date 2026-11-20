package com.wildwest.api.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.booking.BookingDtos.LeadInput;
import com.wildwest.api.booking.BookingDtos.TravelerInput;
import com.wildwest.api.user.User;
import com.wildwest.api.user.UserRepository;
import com.wildwest.api.user.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingExpiryJobTests {

    private static final String TOUR = "cape-town-winelands";

    @Autowired
    BookingExpiryJob job;

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @Autowired
    UserRepository userRepository;

    private User user;
    private Availability departure;
    private int bookedBefore;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("expiry-test-" + UUID.randomUUID() + "@example.test");
        user.setPasswordHash("not-used");
        user.setFirstName("Expiry");
        user.setLastName("Test");
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
    void lapsedHoldIsExpiredAndItsSeatsReleased() {
        BookingDto booking = book(2);
        assertThat(seatsBooked()).isEqualTo(bookedBefore + 2);

        // Not lapsed yet: nothing happens. (Counts aren't asserted: the database may hold
        // other lapsed bookings, which the job rightly expires too.)
        job.expireLapsedHolds(Instant.now());
        assertThat(status(booking)).isEqualTo(Booking.Status.PENDING);
        assertThat(seatsBooked()).isEqualTo(bookedBefore + 2);

        Instant afterHold = booking.expiresAt().plus(1, ChronoUnit.MINUTES);
        job.expireLapsedHolds(afterHold);
        assertThat(status(booking)).isEqualTo(Booking.Status.EXPIRED);
        assertThat(seatsBooked()).isEqualTo(bookedBefore);

        // Running again must not release the seats twice.
        job.expireLapsedHolds(afterHold);
        assertThat(seatsBooked()).isEqualTo(bookedBefore);
    }

    @Test
    void confirmedBookingIsNeverExpired() {
        BookingDto booking = book(1);
        Booking entity = bookingRepository.findByReference(booking.reference()).orElseThrow();
        entity.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(entity);

        job.expireLapsedHolds(booking.expiresAt().plus(1, ChronoUnit.HOURS));
        assertThat(status(booking)).isEqualTo(Booking.Status.CONFIRMED);
        assertThat(seatsBooked()).isEqualTo(bookedBefore + 1);
    }

    private BookingDto book(int travelers) {
        List<TravelerInput> people = java.util.stream.IntStream.range(0, travelers)
                .mapToObj(i -> new TravelerInput("Traveler" + i, "Test", "ADULT"))
                .toList();
        return bookingService.create(
                user,
                new CreateBookingRequest(
                        TOUR,
                        departure.getDepartureDate(),
                        people,
                        new LeadInput("Lead", "Test", "lead@example.test", null, null),
                        null));
    }

    private Booking.Status status(BookingDto booking) {
        return bookingRepository
                .findByReference(booking.reference())
                .orElseThrow()
                .getStatus();
    }

    private int seatsBooked() {
        return availabilityRepository.findById(departure.getId()).orElseThrow().getBooked();
    }
}
