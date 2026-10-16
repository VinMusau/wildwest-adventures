package com.wildwest.api.admin;

import com.wildwest.api.admin.AdminBookingDtos.BookingAdminDetail;
import com.wildwest.api.admin.AdminBookingDtos.BookingAdminSummary;
import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.Booking;
import com.wildwest.api.booking.BookingRepository;
import com.wildwest.api.payment.PaystackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@Transactional
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PaystackService paystackService;

    public AdminBookingService(
            BookingRepository bookingRepository,
            AvailabilityRepository availabilityRepository,
            PaystackService paystackService
    ) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.paystackService = paystackService;
    }

    @Transactional(readOnly = true)
    public Page<BookingAdminSummary> list(
            Booking.Status status,
            String tourSlug,
            String search,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        Specification<Booking> spec = Specification.allOf(
                AdminBookingSpecifications.hasStatus(status),
                AdminBookingSpecifications.hasTourSlug(tourSlug),
                AdminBookingSpecifications.matchesSearch(search),
                AdminBookingSpecifications.createdAfter(from),
                AdminBookingSpecifications.createdBefore(to)
        );
        return bookingRepository.findAll(spec, pageable)
                .map(BookingAdminSummary::from);
    }

    @Transactional(readOnly = true)
    public BookingAdminDetail get(String reference) {
        Booking booking = loadBooking(reference);
        // force lazy loads
        booking.getTravelers().size();
        booking.getAddons().size();
        return BookingAdminDetail.from(booking);
    }

    public BookingAdminDetail cancel(String reference, String reason) {
        Booking booking = loadBooking(reference);

        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already cancelled");
        }
        if (booking.getStatus() == Booking.Status.EXPIRED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Expired bookings cannot be cancelled");
        }

        releaseInventory(booking);

        booking.setStatus(Booking.Status.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancelReason(trimOrNull(reason));

        return BookingAdminDetail.from(bookingRepository.save(booking));
    }

    public BookingAdminDetail confirm(String reference) {
        Booking booking = loadBooking(reference);

        if (booking.getStatus() == Booking.Status.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already confirmed");
        }
        if (booking.getStatus() == Booking.Status.CANCELLED
                || booking.getStatus() == Booking.Status.EXPIRED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot confirm a booking that is " + booking.getStatus());
        }

        booking.setStatus(Booking.Status.CONFIRMED);
        if (booking.getPaidAt() == null) {
            booking.setPaidAt(Instant.now());
        }
        // If it had an expiry, clear it — the hold is now meaningless
        booking.setExpiresAt(null);

        return BookingAdminDetail.from(bookingRepository.save(booking));
    }

    public BookingAdminDetail refund(String reference, Integer amountCents) {
        Booking booking = loadBooking(reference);

        if (booking.getPaidAt() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking has no completed payment to refund");
        }
        if (booking.getRefundedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already refunded");
        }
        if (booking.getPaystackReference() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No Paystack reference — booking was not paid through Paystack");
        }

        int refundAmount = amountCents != null ? amountCents : booking.getTotalCents();
        if (refundAmount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Refund amount must be positive");
        }
        if (refundAmount > booking.getTotalCents()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Refund exceeds paid amount");
        }

        String refundRef = paystackService.refund(
                booking.getPaystackReference(), refundAmount);

        booking.setRefundedAt(Instant.now());
        booking.setRefundReference(refundRef);
        booking.setRefundAmountCents(refundAmount);
        booking.setStatus(Booking.Status.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancelReason("Refunded");

        // Release inventory now that the booking is definitively off the books
        releaseInventory(booking);

        return BookingAdminDetail.from(bookingRepository.save(booking));
    }

    // ---------- internals ----------

    private Booking loadBooking(String reference) {
        return bookingRepository.findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found: " + reference));
    }

    private void releaseInventory(Booking booking) {
        Availability availability = booking.getAvailability();
        if (availability == null) return;
        int newBooked = Math.max(0, availability.getBooked() - booking.getTravelersCount());
        availability.setBooked(newBooked);
        availabilityRepository.save(availability);
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}