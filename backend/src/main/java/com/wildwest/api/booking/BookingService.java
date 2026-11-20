package com.wildwest.api.booking;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.BookingDtos.*;
import com.wildwest.api.tour.Tour;
import com.wildwest.api.tour.TourRepository;
import com.wildwest.api.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private static final int HOLD_MINUTES = 20;
    private static final int ON_REQUEST_MAX_GROUP = 8;

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final TourRepository tourRepository;

    public BookingService(
            BookingRepository bookingRepository,
            AvailabilityRepository availabilityRepository,
            TourRepository tourRepository) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.tourRepository = tourRepository;
    }

    /** How many unpaid bookings (seat holds) one customer may have at once. */
    private static final int MAX_ACTIVE_HOLDS_PER_USER = 3;

    @Transactional
    public BookingDto create(User user, CreateBookingRequest request) {
        // Each unpaid booking holds seats for HOLD_MINUTES; cap how many one customer can hold
        // at once so a single account can't block departures.
        long activeHolds = bookingRepository.countByUserIdAndStatusAndExpiresAtAfter(
                user.getId(), Booking.Status.PENDING, Instant.now());
        if (activeHolds >= MAX_ACTIVE_HOLDS_PER_USER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have " + activeHolds + " unpaid reservations. Complete or let one expire ("
                            + HOLD_MINUTES + " minutes) before reserving another.");
        }

        Tour tour = tourRepository
                .findBySlug(request.tourSlug())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));

        if (request.departureDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That departure date has already passed");
        }

        int travelersCount = request.travelers().size();
        Availability availability;

        if ("ON_REQUEST".equals(tour.getTourType())) {
            // Each on-request booking is its own private group, so several groups can pick
            // the same date. The bookings for a date share one availability row (the table
            // allows one per tour and date); its capacity grows to fit each new group.
            if (travelersCount > ON_REQUEST_MAX_GROUP) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "On-request journeys take up to " + ON_REQUEST_MAX_GROUP + " travelers per booking");
            }
            availabilityRepository.insertOnRequestDateIfMissing(
                    tour.getId(), request.departureDate(), ON_REQUEST_MAX_GROUP, tour.getCurrency());
            availability = availabilityRepository
                    .lockForUpdate(request.tourSlug(), request.departureDate())
                    .orElseThrow();
            availability.setCapacity(Math.max(availability.getCapacity(), availability.getBooked() + travelersCount));
        } else {
            availability = availabilityRepository
                    .lockForUpdate(request.tourSlug(), request.departureDate())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No departure on that date"));
        }

        if (!"OPEN".equals(availability.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Departure is not open");
        }

        if (availability.remaining() < travelersCount) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Only " + availability.remaining() + " places remain on that departure");
        }

        List<AddonCatalog.Addon> selectedAddons = new ArrayList<>();
        if (request.addons() != null) {
            // An add-on applies to the whole booking once; ignore repeats (e.g. a double click)
            // rather than charging for it again.
            for (String code : new java.util.LinkedHashSet<>(request.addons())) {
                try {
                    selectedAddons.add(AddonCatalog.get(code));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown add-on: " + code);
                }
            }
        }

        int pricePerPerson = availability.effectivePriceCents();
        int subtotal = pricePerPerson * travelersCount;
        int addonsTotal = selectedAddons.stream()
                .mapToInt(a -> a.priceCents() * travelersCount)
                .sum();
        int total = subtotal + addonsTotal;

        Booking booking = new Booking();
        booking.setReference(generateUniqueReference());
        booking.setUser(user);
        booking.setTour(tour); // FIXED: Removed duplicate declaration here
        booking.setAvailability(availability);
        booking.setDepartureDate(availability.getDepartureDate());
        booking.setTravelersCount(travelersCount);
        booking.setPriceCentsPerPerson(pricePerPerson);
        booking.setSubtotalCents(subtotal);
        booking.setAddonsTotalCents(addonsTotal);
        booking.setTotalCents(total);
        booking.setCurrency(availability.getCurrency());
        booking.setStatus(Booking.Status.PENDING);

        booking.setLeadFirstName(request.lead().firstName());
        booking.setLeadLastName(request.lead().lastName());
        booking.setLeadEmail(request.lead().email().toLowerCase());
        booking.setLeadPhone(request.lead().phone());
        booking.setLeadCountry(request.lead().country());

        booking.setExpiresAt(Instant.now().plus(HOLD_MINUTES, ChronoUnit.MINUTES));

        for (TravelerInput t : request.travelers()) {
            BookingTraveler traveler = new BookingTraveler();
            traveler.setBooking(booking);
            traveler.setFirstName(t.firstName());
            traveler.setLastName(t.lastName());
            traveler.setTravelerType(parseTravelerType(t.travelerType()));
            booking.getTravelers().add(traveler);
        }

        for (AddonCatalog.Addon a : selectedAddons) {
            BookingAddon addon = new BookingAddon();
            addon.setBooking(booking);
            addon.setCode(a.code());
            addon.setLabel(a.label());
            addon.setPriceCents(a.priceCents() * travelersCount);
            booking.getAddons().add(addon);
        }

        availability.setBooked(availability.getBooked() + travelersCount);
        availabilityRepository.save(availability);

        Booking saved = bookingRepository.save(booking);
        return BookingDto.from(saved);
    }

    @Transactional(readOnly = true)
    public BookingDto getByReference(User user, String reference) {
        Booking booking = bookingRepository
                .findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }

        booking.getTravelers().size();
        booking.getAddons().size();

        return BookingDto.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> listMine(User user) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(booking -> {
                    booking.getTravelers().size();
                    booking.getAddons().size();
                    return BookingDto.from(booking);
                })
                .toList();
    }

    private static BookingTraveler.Type parseTravelerType(String raw) {
        if (raw == null || raw.isBlank()) return BookingTraveler.Type.ADULT;
        try {
            return BookingTraveler.Type.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BookingTraveler.Type.ADULT;
        }
    }

    private String generateUniqueReference() {
        for (int i = 0; i < 5; i++) {
            String ref = BookingReference.generate();
            if (!bookingRepository.existsByReference(ref)) {
                return ref;
            }
        }
        throw new IllegalStateException("Could not generate a unique booking reference");
    }
}
