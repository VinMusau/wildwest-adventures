import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { Destination } from "@/types";

export function useDestinations() {
  return useQuery({
    queryKey: ["destinations"],
    queryFn: () => apiGet<Destination[]>("/v1/destinations"),
    staleTime: 5 * 60_000,
  });
}