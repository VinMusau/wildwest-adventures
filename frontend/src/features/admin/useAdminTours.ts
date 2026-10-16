import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api";
import type { AdminTourDetail, AdminTourSummary, TourWriteInput } from "@/types";

export function useAdminTours() {
  return useQuery({
    queryKey: ["admin", "tours"],
    queryFn: () => apiGet<AdminTourSummary[]>("/v1/admin/tours"),
  });
}

export function useAdminTour(slug: string | undefined) {
  return useQuery({
    queryKey: ["admin", "tours", slug],
    queryFn: () => apiGet<AdminTourDetail>(`/v1/admin/tours/${slug}`),
    enabled: Boolean(slug),
  });
}

export function useCreateTour() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: TourWriteInput) =>
      apiPost<AdminTourDetail>("/v1/admin/tours", input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin", "tours"] });
      qc.invalidateQueries({ queryKey: ["tours"] });
    },
  });
}

export function useUpdateTour(slug: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: TourWriteInput) =>
      apiPut<AdminTourDetail>(`/v1/admin/tours/${slug}`, input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin", "tours"] });
      qc.invalidateQueries({ queryKey: ["tours"] });
    },
  });
}

export function useDeleteTour() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (slug: string) => apiDelete<void>(`/v1/admin/tours/${slug}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin", "tours"] });
      qc.invalidateQueries({ queryKey: ["tours"] });
    },
  });
}