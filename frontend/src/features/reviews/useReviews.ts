import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api";
import type {
  AdminReview,
  RatingSummary,
  Review,
  ReviewStatus,
  SubmitReviewInput,
} from "@/types";

export function useTourReviews(slug: string | undefined) {
  return useQuery({
    queryKey: ["reviews", "tour", slug],
    queryFn: () => apiGet<Review[]>(`/v1/tours/${slug}/reviews`),
    enabled: Boolean(slug),
    staleTime: 60_000,
  });
}

/** The review featured on the homepage, or null when none is featured. */
export function useFeaturedReview() {
  return useQuery({
    queryKey: ["reviews", "featured"],
    queryFn: async () =>
      (await apiGet<Review | undefined>("/v1/reviews/featured")) ?? null,
    staleTime: 60_000,
  });
}

export function useReviewSummary() {
  return useQuery({
    queryKey: ["reviews", "summary"],
    queryFn: () => apiGet<RatingSummary>("/v1/reviews/summary"),
    staleTime: 60_000,
  });
}

export function useSubmitReview(reference: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: SubmitReviewInput) =>
      apiPost<AdminReview>(`/v1/bookings/${reference}/review`, input),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["bookings", "mine"] }),
  });
}

export function useAdminReviews(status: ReviewStatus | undefined) {
  return useQuery({
    queryKey: ["admin", "reviews", status ?? "ALL"],
    queryFn: () =>
      apiGet<AdminReview[]>(
        status ? `/v1/admin/reviews?status=${status}` : "/v1/admin/reviews",
      ),
  });
}

export type ReviewAction = "approve" | "reject" | "feature" | "unfeature";

export function useModerateReview() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, action }: { id: string; action: ReviewAction }) =>
      apiPost<AdminReview>(`/v1/admin/reviews/${id}/${action}`, {}),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin", "reviews"] });
      qc.invalidateQueries({ queryKey: ["reviews"] });
      qc.invalidateQueries({ queryKey: ["tours"] });
      qc.invalidateQueries({ queryKey: ["tour"] });
    },
  });
}
