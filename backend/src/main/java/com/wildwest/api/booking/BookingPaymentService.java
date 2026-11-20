package com.wildwest.api.booking;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one place a successful payment changes a booking. Used by every payment path
 * (Paystack webhook, Paystack verify, Stripe webhook) so they all follow the same rules:
 *
 * <ul>
 *   <li>PENDING: confirmed.</li>
 *   <li>CONFIRMED: nothing to do (duplicate notification).</li>
 *   <li>EXPIRED: the hold lapsed before the money arrived. If the departure still has room,
 *       the seats are taken again and the booking is confirmed. If not, it stays EXPIRED with
 *       the payment recorded, so an admin can refund it.</li>
 *   <li>CANCELLED: stays CANCELLED with the payment recorded, so an admin can refund it.</li>
 * </ul>
 *
 * Confirming never happens without the booking holding its seats, so a late payment can't
 * overbook a departure.
 */
@Service
public class BookingPaymentService {

    private static final Logger log = LoggerFactory.getLogger(BookingPaymentService.class);

    public enum Outcome {
        /** Was PENDING, now CONFIRMED. */
        CONFIRMED,
        /** Was already CONFIRMED; nothing changed. */
        ALREADY_CONFIRMED,
        /** Was EXPIRED, seats were still free, now CONFIRMED. */
        REINSTATED,
        /** Could not be confirmed (cancelled, or expired and full). Payment recorded; needs a refund. */
        PAID_NEEDS_REFUND
    }

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;

    public BookingPaymentService(BookingRepository bookingRepository, AvailabilityRepository availabilityRepository) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional
    public Outcome recordSuccessfulPayment(UUID bookingId, String paymentMethod) {
        Instant now = Instant.now();

        if (bookingRepository.confirmIfStatus(bookingId, Booking.Status.PENDING, now, paymentMethod) == 1) {
            log.info("Booking {} confirmed ({})", referenceOf(bookingId), paymentMethod);
            return Outcome.CONFIRMED;
        }

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        return switch (booking.getStatus()) {
            case CONFIRMED -> {
                log.info(
                        "Booking {} already confirmed, ignoring duplicate payment notification",
                        booking.getReference());
                yield Outcome.ALREADY_CONFIRMED;
            }
            case EXPIRED -> reinstateOrFlag(booking, now, paymentMethod);
            case CANCELLED -> flagForRefund(booking, now, paymentMethod, "it was cancelled");
            // Unreachable: a PENDING booking would have been confirmed above.
            case PENDING ->
                throw new IllegalStateException(
                        "Booking " + booking.getReference() + " is PENDING but could not be confirmed");
        };
    }

    private Outcome reinstateOrFlag(Booking booking, Instant now, String paymentMethod) {
        Availability departure = availabilityRepository
                .lockById(booking.getAvailability().getId())
                .orElseThrow();
        int seats = booking.getTravelersCount();

        if (!"OPEN".equals(departure.getStatus()) || departure.remaining() < seats) {
            return flagForRefund(
                    booking,
                    now,
                    paymentMethod,
                    "its hold expired and the departure no longer has " + seats + " seat(s) free");
        }

        departure.setBooked(departure.getBooked() + seats);
        availabilityRepository.save(departure);
        if (bookingRepository.confirmIfStatus(booking.getId(), Booking.Status.EXPIRED, now, paymentMethod) != 1) {
            // Changed under us; undo the seats by rolling back the whole transaction.
            throw new IllegalStateException("Booking " + booking.getReference() + " changed while being reinstated");
        }
        log.info(
                "Booking {} paid after its hold expired; seats were still free, so it is now confirmed",
                booking.getReference());
        return Outcome.REINSTATED;
    }

    private Outcome flagForRefund(Booking booking, Instant now, String paymentMethod, String why) {
        if (booking.getPaidAt() == null) {
            booking.setPaidAt(now);
        }
        booking.setPaymentMethod(paymentMethod);
        bookingRepository.save(booking);
        log.warn(
                "REFUND NEEDED: booking {} was paid but left {} because {}",
                booking.getReference(),
                booking.getStatus(),
                why);
        return Outcome.PAID_NEEDS_REFUND;
    }

    private String referenceOf(UUID bookingId) {
        return bookingRepository.findById(bookingId).map(Booking::getReference).orElse(bookingId.toString());
    }
}
