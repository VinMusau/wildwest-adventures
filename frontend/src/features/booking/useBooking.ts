import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api";
import type { Booking } from "@/types";

export function useBooking(reference: string | undefined) {
  return useQuery({
    queryKey: ["booking", reference],
    queryFn: () => apiGet<Booking>(`/v1/bookings/${reference}`),
    enabled: Boolean(reference),
    staleTime: 30_000,
  });
}

export function useMyBookings() {
  return useQuery({
    queryKey: ["bookings", "mine"],
    queryFn: () => apiGet<Booking[]>("/v1/bookings/mine"),
    staleTime: 30_000,
  });
}