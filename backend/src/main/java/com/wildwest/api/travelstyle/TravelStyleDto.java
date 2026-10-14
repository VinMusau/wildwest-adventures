package com.wildwest.api.travelstyle;

import java.util.UUID;

public record TravelStyleDto(
        UUID id,
        String slug,
        String title,
        String description,
        String imageUrl,
        int displayOrder
) {
    public static TravelStyleDto from(TravelStyle style) {
        return new TravelStyleDto(
                style.getId(),
                style.getSlug(),
                style.getTitle(),
                style.getDescription(),
                style.getImageUrl(),
                style.getDisplayOrder()
        );
    }
}