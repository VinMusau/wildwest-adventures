import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { Availability } from "@/types";

export function useAvailability(slug: string | undefined, from?: string, to?: string) {
  const params = new URLSearchParams();
  if (from) params.set("from", from);
  if (to) params.set("to", to);
  const qs = params.toString();

  return useQuery({
    queryKey: ["availability", slug, from, to],
    queryFn: () => apiGet<Availability[]>(`/v1/tours/${slug}/availability${qs ? `?${qs}` : ""}`),
    enabled: Boolean(slug),
    staleTime: 30_000,
  });
}