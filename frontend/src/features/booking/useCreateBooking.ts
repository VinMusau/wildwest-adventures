import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiPost } from "@/lib/api";
import type { Booking, CreateBookingInput } from "@/types";

export function useCreateBooking() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateBookingInput) =>
      apiPost<Booking>("/v1/bookings", input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bookings", "mine"] });
    },
  });
}