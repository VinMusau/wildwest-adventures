package com.wildwest.api.booking;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.booking.BookingDtos.LeadInput;
import com.wildwest.api.booking.BookingDtos.TravelerInput;
import com.wildwest.api.tour.Tour;
import com.wildwest.api.tour.TourRepository;
import com.wildwest.api.user.User;
import com.wildwest.api.user.UserRepository;
import com.wildwest.api.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BookingDateRulesTests {

    private static final String SCHEDULED_TOUR = "torres-del-paine";
    private static final String ON_REQUEST_TOUR = "wild-road-south";

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired AvailabilityRepository availabilityRepository;
    @Autowired TourRepository tourRepository;
    @Autowired UserRepository userRepository;

    private User user;
    private String originalTourType;
    private final List<UUID> createdDepartures = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("dates-test-" + UUID.randomUUID() + "@example.test");
        user.setPasswordHash("not-used");
        user.setFirstName("Dates");
        user.setLastName("Test");
        user.setRole(UserRole.USER);
        user = userRepository.save(user);

        Tour onRequest = tourRepository.findBySlug(ON_REQUEST_TOUR).orElseThrow();
        originalTourType = onRequest.getTourType();
        onRequest.setTourType("ON_REQUEST");
        tourRepository.save(onRequest);
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll(bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        userRepository.delete(user);
        availabilityRepository.deleteAllById(createdDepartures);
        Tour onRequest = tourRepository.findBySlug(ON_REQUEST_TOUR).orElseThrow();
        onRequest.setTourType(originalTourType);
        tourRepository.save(onRequest);
    }

    @Test
    void scheduledDepartureThatHasPassedCannotBeBooked() {
        // A departure that has already left but was never closed.
        Availability past = new Availability();
        past.setTour(tourRepository.findBySlug(SCHEDULED_TOUR).orElseThrow());
        past.setDepartureDate(LocalDate.now().minusDays(1000));
        past.setCapacity(8);
        past.setBooked(0);
        past.setCurrency("USD");
        past.setStatus("OPEN");
        createdDepartures.add(availabilityRepository.save(past).getId());

        assertThatThrownBy(() -> book(SCHEDULED_TOUR, past.getDepartureDate(), 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already passed");
    }

    @Test
    void onRequestDateInThePastIsRejected() {
        assertThatThrownBy(() -> book(ON_REQUEST_TOUR, LocalDate.of(2019, 5, 1), 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already passed");
    }

    @Test
    void severalGroupsCanBookTheSameOnRequestDate() {
        LocalDate date = LocalDate.now().plusYears(8);

        BookingDto first = book(ON_REQUEST_TOUR, date, 6);
        BookingDto second = book(ON_REQUEST_TOUR, date, 8);
        trackDeparture(date);

        assertThat(first.status()).isEqualTo("PENDING");
        assertThat(second.status()).isEqualTo("PENDING");
        Availability row = departureOn(date);
        assertThat(row.getBooked()).isEqualTo(14);
        assertThat(row.getCapacity()).isGreaterThanOrEqualTo(14);
    }

    @Test
    void onRequestGroupIsLimitedToEightTravelers() {
        LocalDate date = LocalDate.now().plusYears(8).plusDays(1);
        assertThatThrownBy(() -> book(ON_REQUEST_TOUR, date, 9))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("up to 8 travelers");
    }

    private BookingDto book(String tour, LocalDate date, int travelers) {
        List<TravelerInput> people = IntStream.range(0, travelers)
                .mapToObj(i -> new TravelerInput("Traveler" + i, "Test", "ADULT"))
                .toList();
        return bookingService.create(user, new CreateBookingRequest(
                tour, date, people, new LeadInput("Lead", "Test", "lead@example.test", null, null), null));
    }

    private Availability departureOn(LocalDate date) {
        return availabilityRepository.findByTourSlugOrderByDepartureDateAsc(ON_REQUEST_TOUR).stream()
                .filter(a -> a.getDepartureDate().equals(date))
                .findFirst().orElseThrow();
    }

    private void trackDeparture(LocalDate date) {
        createdDepartures.add(departureOn(date).getId());
    }
}
