package com.wildwest.api.admin;

import com.wildwest.api.booking.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

final class AdminBookingSpecifications {

    private AdminBookingSpecifications() {}

    static Specification<Booking> hasStatus(Booking.Status status) {
        return (root, q, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    static Specification<Booking> hasTourSlug(String slug) {
        return (root, q, cb) -> (slug == null || slug.isBlank())
                ? cb.conjunction()
                : cb.equal(root.get("tour").get("slug"), slug);
    }

    static Specification<Booking> createdAfter(Instant from) {
        return (root, q, cb) -> from == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    static Specification<Booking> createdBefore(Instant to) {
        return (root, q, cb) -> to == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    static Specification<Booking> matchesSearch(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("reference")), like),
                    cb.like(cb.lower(root.get("leadEmail")), like),
                    cb.like(cb.lower(root.get("leadFirstName")), like),
                    cb.like(cb.lower(root.get("leadLastName")), like)
            );
        };
    }
}