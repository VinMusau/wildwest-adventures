import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { TourDetail } from "@/types";

export function useTour(slug: string | undefined) {
  return useQuery({
    queryKey: ["tour", slug],
    queryFn: () => apiGet<TourDetail>(`/v1/tours/${slug}`),
    enabled: Boolean(slug),
    staleTime: 5 * 60_000,
  });
}