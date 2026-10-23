package com.wildwest.api.booking;

import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.booking.BookingDtos.*;
import com.wildwest.api.tour.Tour;
import com.wildwest.api.tour.TourRepository;
import com.wildwest.api.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private static final int HOLD_MINUTES = 20;

    private final BookingRepository bookingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final TourRepository tourRepository;

    public BookingService(
            BookingRepository bookingRepository,
            AvailabilityRepository availabilityRepository,
            TourRepository tourRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.tourRepository = tourRepository;
    }

    @Transactional
    public BookingDto create(User user, CreateBookingRequest request) {
        Tour tour = tourRepository.findBySlug(request.tourSlug())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));

        Availability availability;

        if ("ON_REQUEST".equals(tour.getTourType())) {
            availability = new Availability();
            availability.setTour(tour);
            availability.setDepartureDate(request.departureDate());
            availability.setCapacity(8);
            availability.setBooked(0);
            availability.setStatus("OPEN");
            availability.setCurrency(tour.getCurrency());
            availability = availabilityRepository.save(availability);
        } else {
            availability = availabilityRepository
                    .lockForUpdate(request.tourSlug(), request.departureDate())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "No departure on that date"));
        }

        if (!"OPEN".equals(availability.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Departure is not open");
        }

        int travelersCount = request.travelers().size();
        if (availability.remaining() < travelersCount) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only " + availability.remaining() + " places remain on that departure");
        }

        List<AddonCatalog.Addon> selectedAddons = new ArrayList<>();
        if (request.addons() != null) {
            for (String code : request.addons()) {
                try {
                    selectedAddons.add(AddonCatalog.get(code));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Unknown add-on: " + code);
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
        Booking booking = bookingRepository.findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }

        booking.getTravelers().size();
        booking.getAddons().size();

        return BookingDto.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> listMine(User user) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
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