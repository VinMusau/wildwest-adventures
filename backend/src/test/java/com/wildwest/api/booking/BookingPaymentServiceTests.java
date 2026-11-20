package com.wildwest.api.booking;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.booking.BookingDtos.LeadInput;
import com.wildwest.api.booking.BookingDtos.TravelerInput;
import com.wildwest.api.booking.BookingPaymentService.Outcome;
import com.wildwest.api.user.User;
import com.wildwest.api.user.UserRepository;
import com.wildwest.api.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingPaymentServiceTests {

    private static final String TOUR = "patagonia-beyond-map";

    @Autowired BookingPaymentService paymentService;
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
        user.setEmail("payment-test-" + UUID.randomUUID() + "@example.test");
        user.setPasswordHash("not-used");
        user.setFirstName("Payment");
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
        setSeatsBooked(bookedBefore);
    }

    @Test
    void pendingBookingIsConfirmed() {
        Booking booking = book(2);

        assertThat(paymentService.recordSuccessfulPayment(booking.getId(), "PAYSTACK")).isEqualTo(Outcome.CONFIRMED);

        Booking after = reload(booking);
        assertThat(after.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
        assertThat(after.getPaidAt()).isNotNull();
        assertThat(after.getExpiresAt()).isNull();
        assertThat(seatsBooked()).isEqualTo(bookedBefore + 2);
    }

    @Test
    void duplicateNotificationChangesNothing() {
        Booking booking = book(1);
        paymentService.recordSuccessfulPayment(booking.getId(), "PAYSTACK");

        assertThat(paymentService.recordSuccessfulPayment(booking.getId(), "PAYSTACK"))
                .isEqualTo(Outcome.ALREADY_CONFIRMED);
        assertThat(seatsBooked()).isEqualTo(bookedBefore + 1);
    }

    @Test
    void cancelledBookingStaysCancelledAndIsFlaggedForRefund() {
        Booking booking = book(2);
        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);
        setSeatsBooked(bookedBefore); // cancelling releases the seats

        assertThat(paymentService.recordSuccessfulPayment(booking.getId(), "PAYSTACK"))
                .isEqualTo(Outcome.PAID_NEEDS_REFUND);

        Booking after = reload(booking);
        assertThat(after.getStatus()).isEqualTo(Booking.Status.CANCELLED);
        assertThat(after.getPaidAt()).isNotNull(); // so an admin can refund it
        assertThat(seatsBooked()).isEqualTo(bookedBefore); // no seats taken back
    }

    @Test
    void expiredBookingIsReinstatedWhenSeatsAreStillFree() {
        Booking booking = book(2);
        expire(booking);

        assertThat(paymentService.recordSuccessfulPayment(booking.getId(), "PAYSTACK")).isEqualTo(Outcome.REINSTATED);

        assertThat(reload(booking).getStatus()).isEqualTo(Booking.Status.CONFIRMED);
        assertThat(seatsBooked()).isEqualTo(bookedBefore + 2); // seats taken again
    }

    @Test
    void expiredBookingOnAFullDepartureIsFlaggedForRefund() {
        Booking booking = book(2);
        expire(booking);
        int capacity = availabilityRepository.findById(departure.getId()).orElseThrow().getCapacity();
        setSeatsBooked(capacity); // someone else took the seats

        assertThat(paymentService.recordSuccessfulPayment(booking.getId(), "PAYSTACK"))
                .isEqualTo(Outcome.PAID_NEEDS_REFUND);

        Booking after = reload(booking);
        assertThat(after.getStatus()).isEqualTo(Booking.Status.EXPIRED);
        assertThat(after.getPaidAt()).isNotNull();
        assertThat(seatsBooked()).isEqualTo(capacity); // not overbooked
    }

    private Booking book(int travelers) {
        List<TravelerInput> people = IntStream.range(0, travelers)
                .mapToObj(i -> new TravelerInput("Traveler" + i, "Test", "ADULT"))
                .toList();
        BookingDto dto = bookingService.create(user, new CreateBookingRequest(
                TOUR, departure.getDepartureDate(), people,
                new LeadInput("Lead", "Test", "lead@example.test", null, null), null));
        return bookingRepository.findByReference(dto.reference()).orElseThrow();
    }

    /** What the hold-expiry job does: mark EXPIRED and give the seats back. */
    private void expire(Booking booking) {
        booking.setStatus(Booking.Status.EXPIRED);
        bookingRepository.save(booking);
        setSeatsBooked(seatsBooked() - booking.getTravelersCount());
    }

    private Booking reload(Booking booking) {
        return bookingRepository.findById(booking.getId()).orElseThrow();
    }

    private int seatsBooked() {
        return availabilityRepository.findById(departure.getId()).orElseThrow().getBooked();
    }

    private void setSeatsBooked(int booked) {
        Availability a = availabilityRepository.findById(departure.getId()).orElseThrow();
        a.setBooked(booked);
        availabilityRepository.save(a);
    }
}
