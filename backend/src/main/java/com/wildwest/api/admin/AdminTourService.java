package com.wildwest.api.admin;

import com.wildwest.api.admin.AdminTourDtos.AvailabilityWriteRequest;
import com.wildwest.api.admin.AdminTourDtos.ItineraryDayInput;
import com.wildwest.api.admin.AdminTourDtos.TourAdminDetail;
import com.wildwest.api.admin.AdminTourDtos.TourAdminSummary;
import com.wildwest.api.admin.AdminTourDtos.TourWriteRequest;
import com.wildwest.api.availability.Availability;
import com.wildwest.api.availability.AvailabilityDto;
import com.wildwest.api.availability.AvailabilityRepository;
import com.wildwest.api.destination.Destination;
import com.wildwest.api.destination.DestinationRepository;
import com.wildwest.api.tour.*;
import com.wildwest.api.travelstyle.TravelStyle;
import com.wildwest.api.travelstyle.TravelStyleRepository;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminTourService {

    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private final TravelStyleRepository travelStyleRepository;
    private final AvailabilityRepository availabilityRepository;

    public AdminTourService(
            TourRepository tourRepository,
            DestinationRepository destinationRepository,
            TravelStyleRepository travelStyleRepository,
            AvailabilityRepository availabilityRepository) {
        this.tourRepository = tourRepository;
        this.destinationRepository = destinationRepository;
        this.travelStyleRepository = travelStyleRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional(readOnly = true)
    public List<TourAdminSummary> list() {
        return tourRepository
                .findAll(org.springframework.data.domain.Sort.by("displayOrder").ascending())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourAdminDetail get(String slug) {
        return toDetail(loadTour(slug));
    }

    public TourAdminDetail create(TourWriteRequest req) {
        String slug = (req.slug() != null && !req.slug().isBlank()) ? slugify(req.slug()) : slugify(req.title());

        if (tourRepository.findBySlug(slug).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tour with slug '" + slug + "' already exists");
        }

        Tour tour = new Tour();
        tour.setSlug(slug);
        applyWritableFields(tour, req);
        Tour saved = tourRepository.save(tour);
        return toDetail(saved);
    }

    public TourAdminDetail update(String slug, TourWriteRequest req) {
        Tour tour = loadTour(slug);

        String newSlug = (req.slug() != null && !req.slug().isBlank()) ? slugify(req.slug()) : slugify(req.title());

        if (!newSlug.equals(tour.getSlug())) {
            if (tourRepository.findBySlug(newSlug).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug '" + newSlug + "' is already used");
            }
            tour.setSlug(newSlug);
        }

        applyWritableFields(tour, req);
        Tour saved = tourRepository.save(tour);
        return toDetail(saved);
    }

    public void delete(String slug) {
        Tour tour = loadTour(slug);
        tourRepository.delete(tour);
    }

    private Tour loadTour(String slug) {
        return tourRepository
                .findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + slug));
    }

    private void applyWritableFields(Tour tour, TourWriteRequest req) {
        Destination destination = destinationRepository
                .findBySlug(req.destinationSlug())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unknown destination: " + req.destinationSlug()));

        tour.setTitle(req.title().trim());
        tour.setPlace(req.place().trim());
        tour.setDestination(destination);
        tour.setDurationDays(req.durationDays());
        tour.setPriceCents(req.priceCents());
        tour.setCurrency(req.currency().toUpperCase());
        // rating and reviewCount come from approved reviews (ReviewService), not the form.
        if (tour.getRating() == null) tour.setRating(BigDecimal.ZERO.setScale(1));
        if (tour.getReviewCount() == null) tour.setReviewCount(0);
        tour.setTag(req.tag().trim());
        tour.setImageUrl(req.imageUrl().trim());
        tour.setFeatured(req.featured());
        tour.setDisplayOrder(req.displayOrder());
        tour.setTourType(req.tourType() != null ? req.tourType() : "SCHEDULED");

        Set<TravelStyle> styles = new HashSet<>();
        if (req.styleSlugs() != null) {
            for (String s : req.styleSlugs()) {
                TravelStyle style = travelStyleRepository
                        .findBySlug(s)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown style: " + s));
                styles.add(style);
            }
        }
        tour.getStyles().clear();
        tour.getStyles().addAll(styles);

        tour.getItineraryDays().clear();
        tour.getInclusions().clear();
        if (tour.getId() != null) {
            // Hibernate runs inserts before orphan deletes when it flushes. Without this flush,
            // re-adding an existing day number violates UNIQUE (tour_id, day_number), so saving
            // a tour without changing its itinerary failed.
            tourRepository.flush();
        }

        if (req.itinerary() != null) {
            for (ItineraryDayInput day : req.itinerary()) {
                TourItineraryDay entity = new TourItineraryDay();
                entity.setTour(tour);
                entity.setDayNumber(day.dayNumber());
                entity.setTitle(day.title().trim());
                entity.setDescription(day.description().trim());
                entity.setAccommodation(trimOrNull(day.accommodation()));
                entity.setMealsIncluded(trimOrNull(day.mealsIncluded()));
                tour.getItineraryDays().add(entity);
            }
        }

        if (req.inclusions() != null) {
            int order = 1;
            for (String label : req.inclusions()) {
                if (label == null || label.isBlank()) continue;
                TourInclusion incl = new TourInclusion();
                incl.setTour(tour);
                incl.setLabel(label.trim());
                incl.setDisplayOrder(order++);
                tour.getInclusions().add(incl);
            }
        }
    }

    private TourAdminSummary toSummary(Tour t) {
        return new TourAdminSummary(
                t.getSlug(),
                t.getTitle(),
                t.getPlace(),
                t.getDestination().getSlug(),
                t.getDestination().getName(),
                t.getDurationDays(),
                t.getPriceCents(),
                t.getCurrency(),
                Boolean.TRUE.equals(t.getFeatured()),
                t.getDisplayOrder());
    }

    private TourAdminDetail toDetail(Tour t) {
        List<ItineraryDayInput> itinerary = t.getItineraryDays().stream()
                .sorted(Comparator.comparing(TourItineraryDay::getDayNumber))
                .map(d -> new ItineraryDayInput(
                        d.getDayNumber(), d.getTitle(), d.getDescription(), d.getAccommodation(), d.getMealsIncluded()))
                .collect(Collectors.toList());

        List<String> inclusions = t.getInclusions().stream()
                .sorted(Comparator.comparing(TourInclusion::getDisplayOrder))
                .map(TourInclusion::getLabel)
                .toList();

        List<String> styleSlugs =
                t.getStyles().stream().map(TravelStyle::getSlug).sorted().toList();

        return new TourAdminDetail(
                t.getSlug(),
                t.getTitle(),
                t.getPlace(),
                t.getDestination().getSlug(),
                t.getDurationDays(),
                t.getPriceCents(),
                t.getCurrency(),
                t.getRating(),
                t.getReviewCount(),
                t.getTag(),
                t.getImageUrl(),
                Boolean.TRUE.equals(t.getFeatured()),
                t.getDisplayOrder(),
                styleSlugs,
                itinerary,
                inclusions,
                t.getTourType());
    }

    private static String slugify(String input) {
        String normalized =
                Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-{2,}", "-");
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> listAvailability(String slug) {
        loadTour(slug); // 404 for an unknown tour instead of an empty list
        return availabilityRepository.findByTourSlugOrderByDepartureDateAsc(slug).stream()
                .map(AvailabilityDto::from)
                .toList();
    }

    public AvailabilityDto addAvailability(String slug, AvailabilityWriteRequest req) {
        Tour tour = loadTour(slug);
        LocalDate date = req.date();
        Integer capacity = req.capacity();

        boolean exists = availabilityRepository.findByTourSlugOrderByDepartureDateAsc(slug).stream()
                .anyMatch(a -> a.getDepartureDate().equals(date));
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This tour already has a departure on " + date);
        }

        Availability av = new Availability();
        av.setTour(tour);
        av.setDepartureDate(date);
        av.setCapacity(capacity);
        av.setBooked(0);
        av.setStatus("OPEN");
        av.setCurrency(tour.getCurrency());
        // priceCents is null, so it falls back to the Tour's price

        return AvailabilityDto.from(availabilityRepository.save(av));
    }

    public void deleteAvailability(UUID id) {
        Availability av = availabilityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Date not found"));

        if (av.getBooked() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete a date that already has bookings");
        }
        availabilityRepository.delete(av);
    }
}
