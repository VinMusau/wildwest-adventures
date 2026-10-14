package com.wildwest.api.destination;

import java.util.UUID;

public record DestinationDto(
        UUID id,
        String slug,
        String name,
        String imageUrl,
        int journeyCount,
        int displayOrder
) {
    public static DestinationDto from(Destination destination) {
        return new DestinationDto(
                destination.getId(),
                destination.getSlug(),
                destination.getName(),
                destination.getImageUrl(),
                destination.getJourneyCount(),
                destination.getDisplayOrder()
        );
    }
}