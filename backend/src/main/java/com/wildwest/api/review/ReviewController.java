package com.wildwest.api.review;

import com.wildwest.api.review.ReviewDtos.AdminReview;
import com.wildwest.api.review.ReviewDtos.PublicReview;
import com.wildwest.api.review.ReviewDtos.RatingSummary;
import com.wildwest.api.review.ReviewDtos.SubmitReviewRequest;
import com.wildwest.api.user.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    /** The customer reviews their own trip. Pending until an admin approves it. */
    @PostMapping("/bookings/{reference}/review")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminReview submit(
            @AuthenticationPrincipal User user,
            @PathVariable String reference,
            @Valid @RequestBody SubmitReviewRequest request) {
        return service.submit(user, reference, request);
    }

    @GetMapping("/tours/{slug}/reviews")
    public List<PublicReview> forTour(@PathVariable String slug) {
        return service.approvedForTour(slug);
    }

    /** The review featured on the homepage; 204 when none is featured. */
    @GetMapping("/reviews/featured")
    public ResponseEntity<PublicReview> featured() {
        return service.featured()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/reviews/summary")
    public RatingSummary summary() {
        return service.summary();
    }
}
