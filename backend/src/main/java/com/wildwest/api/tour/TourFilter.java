package com.wildwest.api.tour;

public record TourFilter(
        String destination,
        String style,
        Integer minPrice,
        Integer maxPrice,
        Integer minDays,
        Integer maxDays,
        String q,
        Boolean featured,
        String sort
) {}