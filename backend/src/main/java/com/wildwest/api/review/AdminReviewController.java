package com.wildwest.api.review;

import com.wildwest.api.review.ReviewDtos.AdminReview;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService service;

    public AdminReviewController(ReviewService service) {
        this.service = service;
    }

    /** All reviews, newest first, optionally filtered by status (PENDING, APPROVED, REJECTED). */
    @GetMapping
    public List<AdminReview> list(@RequestParam(required = false) Review.Status status) {
        return service.list(status);
    }

    @PostMapping("/{id}/approve")
    public AdminReview approve(@PathVariable UUID id) {
        return service.approve(id);
    }

    /** Rejects a pending review, or unpublishes an approved one. */
    @PostMapping("/{id}/reject")
    public AdminReview reject(@PathVariable UUID id) {
        return service.reject(id);
    }

    @PostMapping("/{id}/feature")
    public AdminReview feature(@PathVariable UUID id) {
        return service.setFeatured(id, true);
    }

    @PostMapping("/{id}/unfeature")
    public AdminReview unfeature(@PathVariable UUID id) {
        return service.setFeatured(id, false);
    }
}
