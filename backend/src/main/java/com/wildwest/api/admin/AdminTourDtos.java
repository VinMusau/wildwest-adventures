package com.wildwest.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public final class AdminTourDtos {

    private AdminTourDtos() {}

    public record ItineraryDayInput(
            @NotNull @Min(1) Integer dayNumber,
            @NotBlank @Size(max = 200) String title,
            @NotBlank String description,
            @Size(max = 200) String accommodation,
            @Size(max = 200) String mealsIncluded
    ) {}

    public record TourWriteRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 140) String slug,             // optional, auto-generated from title
            @NotBlank @Size(max = 160) String place,
            @NotBlank String destinationSlug,
            @NotNull @Min(1) @Max(90) Integer durationDays,
            @NotNull @Min(0) Integer priceCents,
            @NotBlank @Size(min = 3, max = 3) String currency,
            @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating,
            @NotNull @Min(0) Integer reviewCount,
            @NotBlank @Size(max = 60) String tag,
            @NotBlank @Size(max = 500) String imageUrl,
            @NotNull Boolean featured,
            @NotNull @Min(0) Integer displayOrder,
            List<String> styleSlugs,
            @Valid List<ItineraryDayInput> itinerary,
            List<String> inclusions
    ) {}

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
            int displayOrder
    ) {}

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
            List<String> inclusions
    ) {}
}
