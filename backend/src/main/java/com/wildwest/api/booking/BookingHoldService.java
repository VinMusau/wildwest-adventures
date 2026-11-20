package com.wildwest.api.booking;

import com.wildwest.api.availability.AvailabilityRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Ends seat holds on bookings that were never paid. */
@Service
public class BookingHoldService {

    private static final Logger log = LoggerFactory.getLogger(BookingHoldService.class);

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;

    public BookingHoldService(BookingRepository bookingRepository, AvailabilityRepository availabilityRepository) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
    }

    /**
     * Expires one booking and gives its seats back to the departure, in its own transaction
     * so one failure doesn't roll back the rest of a batch.
     *
     * @return true if the booking was expired by this call
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean expire(UUID bookingId, Instant now) {
        if (bookingRepository.expireIfPending(bookingId, now) == 0) {
            return false; // paid, cancelled or already expired in the meantime
        }
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        availabilityRepository.releaseSeats(booking.getAvailability().getId(), booking.getTravelersCount());
        log.info("Booking {} expired; released {} seat(s)", booking.getReference(), booking.getTravelersCount());
        return true;
    }
}
