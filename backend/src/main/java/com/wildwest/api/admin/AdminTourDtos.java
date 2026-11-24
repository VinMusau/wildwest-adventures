package com.wildwest.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class AdminTourDtos {

    private AdminTourDtos() {}

    public record ItineraryDayInput(
            @NotNull @Min(1) Integer dayNumber,
            @NotBlank @Size(max = 200) String title,
            @NotBlank String description,
            @Size(max = 200) String accommodation,
            @Size(max = 200) String mealsIncluded) {}

    /** A gallery photo; the list's order is the display order. */
    public record TourImageInput(
            @NotBlank @Size(max = 500) String imageUrl,
            @Size(max = 200) String altText) {}

    public record TourWriteRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 140) String slug, // optional, auto-generated from title
            @NotBlank @Size(max = 160) String place,
            @NotBlank String destinationSlug,
            @NotNull @Min(1) @Max(90) Integer durationDays,
            @NotNull @Min(0) Integer priceCents,
            @NotBlank @Size(min = 3, max = 3) String currency,
            @NotBlank @Size(max = 60) String tag,
            @NotBlank @Size(max = 500) String imageUrl,
            @NotNull Boolean featured,
            @NotNull @Min(0) Integer displayOrder,
            List<String> styleSlugs,
            List<@Valid ItineraryDayInput> itinerary,
            List<String> inclusions,
            String tourType,
            // Gallery photos in display order. Replaces the gallery; leave out (null) to keep it.
            @Size(max = 30) List<@Valid TourImageInput> images) {}

    public record AvailabilityWriteRequest(
            @NotNull @FutureOrPresent LocalDate date,
            @NotNull @Min(1) Integer capacity) {}

    public record TourAdminSummary(
            String slug,
            String title,
            String place,
            String destinationSlug,
            String destinationName,
            int durationDays,
            int priceCents,
            String currency,
            boolean featured,
            int displayOrder) {}

    public record TourAdminDetail(
            String slug,
            String title,
            String place,
            String destinationSlug,
            int durationDays,
            int priceCents,
            String currency,
            BigDecimal rating,
            int reviewCount,
            String tag,
            String imageUrl,
            boolean featured,
            int displayOrder,
            List<String> styleSlugs,
            List<ItineraryDayInput> itinerary,
            List<String> inclusions,
            String tourType,
            List<TourImageInput> images) {}
}
