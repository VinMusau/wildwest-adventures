package com.wildwest.api.mail;

import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingEvents;
import com.wildwest.api.booking.BookingRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Emails the booking's lead traveler when a booking is confirmed or cancelled.
 *
 * <p>Runs after the transaction that changed the booking has committed (so nothing is sent for a
 * change that rolled back) and on a background thread (so a slow or failing mail server never
 * delays or breaks the booking). Failures are logged, not retried.
 *
 * <p>Off unless {@code wildwest.mail.enabled=true} and {@code spring.mail.host} is set.
 */
@Service
public class BookingEmailService {

    private static final Logger log = LoggerFactory.getLogger(BookingEmailService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH);

    private final BookingRepository bookingRepository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean enabled;
    private final String from;
    private final String appUrl;

    public BookingEmailService(
            BookingRepository bookingRepository,
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${wildwest.mail.enabled:false}") boolean enabled,
            @Value("${wildwest.mail.from:}") String from,
            @Value("${wildwest.app-url:http://localhost:5173}") String appUrl) {
        this.bookingRepository = bookingRepository;
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.appUrl = appUrl.replaceAll("/+$", "");
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onConfirmed(BookingEvents.Confirmed event) {
        send(
                event.bookingId(),
                b -> new Email(
                        "Booking confirmed: " + b.getTour().getTitle() + " (" + b.getReference() + ")", """
                Hi %s,

                Your booking is confirmed. We can't wait to travel with you.

                Journey:     %s
                Departure:   %s
                Travellers:  %d
                Total paid:  %s
                Reference:   %s

                You can see your booking any time at %s/account/bookings

                Wild West Adventures
                """.formatted(
                                        b.getLeadFirstName(),
                                        b.getTour().getTitle(),
                                        DATE.format(b.getDepartureDate()),
                                        b.getTravelersCount(),
                                        money(b.getTotalCents(), b.getCurrency()),
                                        b.getReference(),
                                        appUrl)));
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onCancelled(BookingEvents.Cancelled event) {
        send(
                event.bookingId(),
                b -> new Email(
                        "Booking cancelled: " + b.getTour().getTitle() + " (" + b.getReference() + ")", """
                Hi %s,

                Your booking %s for %s, departing %s, has been cancelled.%s
                %s
                If you have any questions, just reply to this email.

                Wild West Adventures
                """.formatted(
                                        b.getLeadFirstName(),
                                        b.getReference(),
                                        b.getTour().getTitle(),
                                        DATE.format(b.getDepartureDate()),
                                        b.getCancelReason() != null ? "\n\nReason: " + b.getCancelReason() : "",
                                        b.getPaidAt() != null && b.getRefundedAt() == null
                                                ? "\nAs you'd already paid, we'll refund you and email again when it's on its way.\n"
                                                : "")));
    }

    private record Email(String subject, String body) {}

    private void send(UUID bookingId, java.util.function.Function<Booking, Email> compose) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!enabled || sender == null) {
            return;
        }
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return;
        }
        try {
            Email email = compose.apply(booking);
            SimpleMailMessage message = new SimpleMailMessage();
            if (!from.isBlank()) message.setFrom(from);
            message.setTo(booking.getLeadEmail());
            message.setSubject(email.subject());
            message.setText(email.body());
            sender.send(message);
            log.info("Sent '{}' to {}", email.subject(), booking.getLeadEmail());
        } catch (RuntimeException e) {
            log.error("Could not email booking {} to {}", booking.getReference(), booking.getLeadEmail(), e);
        }
    }

    private static String money(int cents, String currency) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);
        try {
            f.setCurrency(Currency.getInstance(currency));
        } catch (IllegalArgumentException ignored) {
            // unknown code: keep the default symbol
        }
        return f.format(BigDecimal.valueOf(cents, 2));
    }
}
