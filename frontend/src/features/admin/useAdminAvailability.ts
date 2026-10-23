import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiDelete } from "@/lib/api";
import type { Availability } from "@/types";

export function useAdminAvailability(slug: string | undefined) {
  return useQuery({
    queryKey: ["admin", "availability", slug],
    queryFn: () => apiGet<Availability[]>(`/v1/admin/tours/${slug}/availability`),
    enabled: Boolean(slug) && slug !== "new",
  });
}

export function useAddAvailability(slug: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { date: string; capacity: number }) =>
      apiPost<Availability>(`/v1/admin/tours/${slug}/availability`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin", "availability", slug] }),
  });
}

export function useDeleteAvailability(slug: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiDelete<void>(`/v1/admin/tours/availability/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin", "availability", slug] }),
  });
}