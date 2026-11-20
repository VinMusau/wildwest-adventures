package com.wildwest.api.mail;

import static org.assertj.core.api.Assertions.assertThat;

import com.wildwest.api.admin.AdminBookingService;
import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingDtos.BookingDto;
import com.wildwest.api.booking.BookingDtos.CreateBookingRequest;
import com.wildwest.api.booking.BookingDtos.LeadInput;
import com.wildwest.api.booking.BookingDtos.TravelerInput;
import com.wildwest.api.booking.BookingPaymentService;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.booking.BookingService;
import com.wildwest.api.user.User;
import com.wildwest.api.user.UserRepository;
import com.wildwest.api.user.UserRole;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootTest(properties = {"wildwest.mail.enabled=true", "wildwest.mail.from=bookings@example.test"})
class BookingEmailTests {

    /** Records messages instead of sending them. */
    static class RecordingMailSender extends JavaMailSenderImpl {
        final List<SimpleMailMessage> sent = new CopyOnWriteArrayList<>();

        @Override
        public void send(SimpleMailMessage message) {
            sent.add(message);
        }

        @Override
        public void send(SimpleMailMessage... messages) {
            sent.addAll(List.of(messages));
        }
    }

    @TestConfiguration
    static class MailConfig {
        @Bean
        @Primary
        RecordingMailSender recordingMailSender() {
            return new RecordingMailSender();
        }
    }

    private static final String TOUR = "okavango-elephants";

    @Autowired
    RecordingMailSender mail;

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingPaymentService paymentService;

    @Autowired
    AdminBookingService adminBookingService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @Autowired
    UserRepository userRepository;

    private User user;
    private Availability departure;
    private int bookedBefore;
    private String leadEmail;

    @BeforeEach
    void setUp() {
        mail.sent.clear();
        user = new User();
        user.setEmail("email-test-" + UUID.randomUUID() + "@example.test");
        user.setPasswordHash("not-used");
        user.setFirstName("Email");
        user.setLastName("Test");
        user.setRole(UserRole.USER);
        user = userRepository.save(user);
        leadEmail = "lead-" + UUID.randomUUID() + "@example.test";
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
    void paymentSendsAConfirmationToTheLeadTraveler() throws InterruptedException {
        BookingDto booking = book();

        paymentService.recordSuccessfulPayment(booking.id(), "PAYSTACK");

        SimpleMailMessage email = awaitEmail();
        assertThat(email.getTo()).containsExactly(leadEmail);
        assertThat(email.getFrom()).isEqualTo("bookings@example.test");
        assertThat(email.getSubject()).startsWith("Booking confirmed").contains(booking.reference());
        assertThat(email.getText())
                .contains("Hi Ann", "Elephants of the Okavango", booking.reference(), "/account/bookings");
    }

    @Test
    void adminCancellationSendsACancellationWithTheReason() throws InterruptedException {
        BookingDto booking = book();

        adminBookingService.cancel(booking.reference(), "Flooding on the route");

        SimpleMailMessage email = awaitEmail();
        assertThat(email.getSubject()).startsWith("Booking cancelled").contains(booking.reference());
        assertThat(email.getText()).contains("Reason: Flooding on the route");
    }

    @Test
    void paymentThatCannotConfirmTheBookingSendsNoConfirmation() throws InterruptedException {
        BookingDto booking = book();
        Booking entity = bookingRepository.findByReference(booking.reference()).orElseThrow();
        entity.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(entity);

        paymentService.recordSuccessfulPayment(booking.id(), "PAYSTACK");

        Thread.sleep(1500);
        assertThat(mail.sent).isEmpty();
    }

    private BookingDto book() {
        return bookingService.create(
                user,
                new CreateBookingRequest(
                        TOUR,
                        departure.getDepartureDate(),
                        List.of(new TravelerInput("Ann", "Lee", "ADULT")),
                        new LeadInput("Ann", "Lee", leadEmail, null, null),
                        null));
    }

    /** Emails are sent on a background thread after commit; wait up to 5 s for one. */
    private SimpleMailMessage awaitEmail() throws InterruptedException {
        for (int i = 0; i < 50 && mail.sent.isEmpty(); i++) Thread.sleep(100);
        assertThat(mail.sent).hasSize(1);
        return mail.sent.get(0);
    }
}
