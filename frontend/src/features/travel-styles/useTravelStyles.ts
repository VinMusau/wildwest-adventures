import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { TravelStyle } from "@/types";

export function useTravelStyles() {
  return useQuery({
    queryKey: ["travel-styles"],
    queryFn: () => apiGet<TravelStyle[]>("/v1/travel-styles"),
    staleTime: 5 * 60_000,
  });
}