import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { Tour } from "@/types";

export interface TourFilters {
  destination?: string;
  style?: string;
  minPrice?: number;
  maxPrice?: number;
  minDays?: number;
  maxDays?: number;
  q?: string;
  featured?: boolean;
  sort?: string;
}

export function useTours(filters: TourFilters = {}) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });
  const qs = params.toString();

  return useQuery({
    queryKey: ["tours", filters],
    queryFn: () => apiGet<Tour[]>(`/v1/tours${qs ? `?${qs}` : ""}`),
    staleTime: 5 * 60_000,
  });
}