package com.wildwest.api.tour;

import com.wildwest.api.travelstyle.TravelStyle;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TourSummaryDto(
        UUID id,
        String slug,
        String title,
        String place,
        String destinationSlug,
        String destinationName,
        int durationDays,
        int priceCents,
        String currency,
        BigDecimal rating,
        int reviewCount,
        String tag,
        String imageUrl,
        boolean featured,
        List<String> styleSlugs
) {
    public static TourSummaryDto from(Tour tour) {
        return new TourSummaryDto(
                tour.getId(),
                tour.getSlug(),
                tour.getTitle(),
                tour.getPlace(),
                tour.getDestination().getSlug(),
                tour.getDestination().getName(),
                tour.getDurationDays(),
                tour.getPriceCents(),
                tour.getCurrency(),
                tour.getRating(),
                tour.getReviewCount(),
                tour.getTag(),
                tour.getImageUrl(),
                Boolean.TRUE.equals(tour.getFeatured()),
                tour.getStyles().stream().map(TravelStyle::getSlug).sorted().toList()
        );
    }
}