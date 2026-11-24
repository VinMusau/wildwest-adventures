package com.wildwest.api.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ReviewDtos {

    private ReviewDtos() {}

    public record SubmitReviewRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 120) String title,
            @NotBlank @Size(min = 20, max = 2000) String body) {}

    /** A published review, as anyone can see it. */
    public record PublicReview(
            UUID id,
            int rating,
            String title,
            String body,
            String authorName,
            String authorCountry,
            String tourSlug,
            String tourTitle,
            LocalDate travelledOn,
            Instant createdAt) {

        public static PublicReview from(Review r) {
            return new PublicReview(
                    r.getId(),
                    r.getRating(),
                    r.getTitle(),
                    r.getBody(),
                    r.getAuthorName(),
                    r.getAuthorCountry(),
                    r.getTour().getSlug(),
                    r.getTour().getTitle(),
                    r.getBooking().getDepartureDate(),
                    r.getCreatedAt());
        }
    }

    /** Average and count of approved reviews; average is null when there are none. */
    public record RatingSummary(BigDecimal averageRating, long count) {

        public static RatingSummary from(ReviewRepository.Stats stats) {
            long count = stats.count() == null ? 0 : stats.count();
            BigDecimal average =
                    count == 0 ? null : BigDecimal.valueOf(stats.average()).setScale(1, RoundingMode.HALF_UP);
            return new RatingSummary(average, count);
        }
    }

    /** A review as admins see it, with moderation details. */
    public record AdminReview(
            UUID id,
            String status,
            boolean featured,
            int rating,
            String title,
            String body,
            String authorName,
            String authorCountry,
            String customerEmail,
            String bookingReference,
            String tourSlug,
            String tourTitle,
            LocalDate travelledOn,
            Instant createdAt,
            Instant moderatedAt) {

        public static AdminReview from(Review r) {
            return new AdminReview(
                    r.getId(),
                    r.getStatus().name(),
                    r.isFeatured(),
                    r.getRating(),
                    r.getTitle(),
                    r.getBody(),
                    r.getAuthorName(),
                    r.getAuthorCountry(),
                    r.getUser().getEmail(),
                    r.getBooking().getReference(),
                    r.getTour().getSlug(),
                    r.getTour().getTitle(),
                    r.getBooking().getDepartureDate(),
                    r.getCreatedAt(),
                    r.getModeratedAt());
        }
    }
}
