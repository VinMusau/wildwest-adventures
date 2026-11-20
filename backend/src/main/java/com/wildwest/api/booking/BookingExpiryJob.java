package com.wildwest.api.booking;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Releases the seats held by bookings that weren't paid within the hold window
 * (BookingService.HOLD_MINUTES). Without this, every abandoned checkout would keep
 * its seats forever and departures would look sold out.
 */
@Component
public class BookingExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryJob.class);

    private final BookingRepository bookingRepository;
    private final BookingHoldService holdService;

    public BookingExpiryJob(BookingRepository bookingRepository, BookingHoldService holdService) {
        this.bookingRepository = bookingRepository;
        this.holdService = holdService;
    }

    @Scheduled(
            initialDelayString = "${wildwest.booking.expiry-interval-ms:60000}",
            fixedDelayString = "${wildwest.booking.expiry-interval-ms:60000}")
    public void run() {
        expireLapsedHolds(Instant.now());
    }

    /** @return how many bookings were expired */
    public int expireLapsedHolds(Instant now) {
        int expired = 0;
        for (UUID id : bookingRepository.findExpiredPendingIds(now)) {
            try {
                if (holdService.expire(id, now)) expired++;
            } catch (RuntimeException e) {
                log.error("Could not expire booking {}", id, e);
            }
        }
        if (expired > 0) {
            log.info("Expired {} lapsed booking hold(s)", expired);
        }
        return expired;
    }
}
