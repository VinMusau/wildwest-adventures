package com.wildwest.api.tour;

import org.springframework.data.jpa.domain.Specification;

final class TourSpecifications {

    private TourSpecifications() {}

    static Specification<Tour> destinationIs(String slug) {
        return (root, query, cb) -> (slug == null || slug.isBlank())
                ? cb.conjunction()
                : cb.equal(root.get("destination").get("slug"), slug);
    }

    static Specification<Tour> hasStyle(String slug) {
        return (root, query, cb) -> {
            if (slug == null || slug.isBlank()) return cb.conjunction();
            return cb.equal(root.join("styles").get("slug"), slug);
        };
    }

    static Specification<Tour> priceAtLeast(Integer cents) {
        return (root, query, cb) -> cents == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("priceCents"), cents);
    }

    static Specification<Tour> priceAtMost(Integer cents) {
        return (root, query, cb) -> cents == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("priceCents"), cents);
    }

    static Specification<Tour> durationAtLeast(Integer days) {
        return (root, query, cb) -> days == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("durationDays"), days);
    }

    static Specification<Tour> durationAtMost(Integer days) {
        return (root, query, cb) -> days == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("durationDays"), days);
    }

    static Specification<Tour> matchesText(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("place")), pattern)
            );
        };
    }

    static Specification<Tour> isFeatured(Boolean featured) {
        return (root, query, cb) -> featured == null
                ? cb.conjunction()
                : cb.equal(root.get("featured"), featured);
    }
}