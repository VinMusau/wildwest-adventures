package com.wildwest.api.tour;

import com.wildwest.api.travelstyle.TravelStyle;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TourDetailDto(
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
        List<String> styleSlugs,
        List<ItineraryDayDto> itinerary,
        List<String> inclusions,
        List<TourImageDto> images
) {

    public record ItineraryDayDto(
            int dayNumber,
            String title,
            String description,
            String accommodation,
            String mealsIncluded
    ) {
        public static ItineraryDayDto from(TourItineraryDay day) {
            return new ItineraryDayDto(
                    day.getDayNumber(),
                    day.getTitle(),
                    day.getDescription(),
                    day.getAccommodation(),
                    day.getMealsIncluded()
            );
        }
    }

    public record TourImageDto(String imageUrl, String altText) {
        public static TourImageDto from(TourImage image) {
            return new TourImageDto(image.getImageUrl(), image.getAltText());
        }
    }

    public static TourDetailDto from(Tour tour) {
        return new TourDetailDto(
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
                tour.getStyles().stream().map(TravelStyle::getSlug).sorted().toList(),
                tour.getItineraryDays().stream().map(ItineraryDayDto::from).toList(),
                tour.getInclusions().stream().map(TourInclusion::getLabel).toList(),
                tour.getImages().stream().map(TourImageDto::from).toList()
        );
    }
}