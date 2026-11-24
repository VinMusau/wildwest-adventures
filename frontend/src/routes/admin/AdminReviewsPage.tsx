import { useState } from "react";
import { Link } from "react-router";
import Stars from "@/components/review/Stars";
import { travelledLabel } from "@/components/review/ReviewCard";
import AdminTableSkeleton from "@/components/admin/AdminTableSkeleton";
import {
  useAdminReviews,
  useModerateReview,
  type ReviewAction,
} from "@/features/reviews/useReviews";
import { useToast } from "@/features/toast/useToast";
import { ApiError } from "@/lib/api";
import type { AdminReview, ReviewStatus } from "@/types";

const FILTERS: Array<[ReviewStatus | undefined, string]> = [
  ["PENDING", "Waiting for approval"],
  ["APPROVED", "Published"],
  ["REJECTED", "Rejected"],
  [undefined, "All"],
];

const STATUS: Record<ReviewStatus, { label: string; className: string }> = {
  PENDING: { label: "Pending", className: "status-pending" },
  APPROVED: { label: "Published", className: "status-confirmed" },
  REJECTED: { label: "Rejected", className: "status-cancelled" },
};

const DONE: Record<ReviewAction, string> = {
  approve: "Review published",
  reject: "Review hidden",
  feature: "Featured on the homepage",
  unfeature: "Removed from the homepage",
};

export default function AdminReviewsPage() {
  const [status, setStatus] = useState<ReviewStatus | undefined>("PENDING");
  const { data: reviews = [], isLoading } = useAdminReviews(status);
  const moderate = useModerateReview();
  const toast = useToast();

  const act = (review: AdminReview, action: ReviewAction) =>
    moderate.mutate(
      { id: review.id, action },
      {
        onSuccess: () => toast.success(DONE[action]),
        onError: (err) =>
          toast.error("Could not update review", {
            description:
              err instanceof ApiError ? err.message : "Please try again.",
          }),
      },
    );

  return (
    <div className="admin-page">
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">Verified guests</span>
          <h1>Reviews</h1>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            Only customers whose confirmed trip has ended can review it. Reviews
            appear on the site once you publish them; the featured one is shown
            on the homepage.
          </p>
        </div>
      </div>

      <div className="admin-filter-bar">
        <select
          value={status ?? ""}
          onChange={(e) =>
            setStatus((e.target.value || undefined) as ReviewStatus | undefined)
          }
        >
          {FILTERS.map(([value, label]) => (
            <option key={label} value={value ?? ""}>
              {label}
            </option>
          ))}
        </select>
      </div>

      {isLoading ? (
        <AdminTableSkeleton rows={4} cols={3} />
      ) : reviews.length === 0 ? (
        <p className="text-muted">No reviews here.</p>
      ) : (
        <div className="review-list">
          {reviews.map((r) => (
            <article className="review-card" key={r.id}>
              <div className="admin-review-head">
                <Stars rating={r.rating} />
                <span className={`status-badge ${STATUS[r.status].className}`}>
                  {STATUS[r.status].label}
                </span>
                {r.featured && (
                  <span className="status-badge status-needs-refund">
                    Featured
                  </span>
                )}
              </div>
              {r.title && <h3>{r.title}</h3>}
              <p>{r.body}</p>
              <div className="review-meta">
                <strong>{r.authorName}</strong>
                <span> · {r.customerEmail}</span>
                <span>
                  {" "}
                  · <Link to={`/journeys/${r.tourSlug}`}>{r.tourTitle}</Link>
                </span>
                <span> · Travelled {travelledLabel(r.travelledOn)}</span>
                <span> · Booking {r.bookingReference}</span>
              </div>
              <div className="admin-review-actions">
                {r.status !== "APPROVED" && (
                  <button
                    className="button primary"
                    disabled={moderate.isPending}
                    onClick={() => act(r, "approve")}
                  >
                    Publish
                  </button>
                )}
                {r.status === "APPROVED" &&
                  (r.featured ? (
                    <button
                      className="button ghost"
                      disabled={moderate.isPending}
                      onClick={() => act(r, "unfeature")}
                    >
                      Remove from homepage
                    </button>
                  ) : (
                    <button
                      className="button ghost"
                      disabled={moderate.isPending}
                      onClick={() => act(r, "feature")}
                    >
                      Feature on homepage
                    </button>
                  ))}
                {r.status !== "REJECTED" && (
                  <button
                    className="button ghost"
                    disabled={moderate.isPending}
                    onClick={() => act(r, "reject")}
                  >
                    {r.status === "APPROVED" ? "Unpublish" : "Reject"}
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
