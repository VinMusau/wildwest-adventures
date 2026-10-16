import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api";
import type {
  AdminBookingFilters,
  AdminBookingsPage,
  BookingAdminDetail,
} from "@/types";

export function useAdminBookings(filters: AdminBookingFilters) {
  const params = new URLSearchParams();
  if (filters.status) params.set("status", filters.status);
  if (filters.tour) params.set("tour", filters.tour);
  if (filters.q) params.set("q", filters.q);
  params.set("page", String(filters.page ?? 0));
  params.set("size", String(filters.size ?? 25));

  return useQuery({
    queryKey: ["admin", "bookings", filters],
    queryFn: () =>
      apiGet<AdminBookingsPage>(`/v1/admin/bookings?${params.toString()}`),
    placeholderData: (prev) => prev,
  });
}

export function useAdminBooking(reference: string | undefined) {
  return useQuery({
    queryKey: ["admin", "bookings", "detail", reference],
    queryFn: () =>
      apiGet<BookingAdminDetail>(`/v1/admin/bookings/${reference}`),
    enabled: Boolean(reference),
  });
}

export function useConfirmBooking(reference: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () =>
      apiPost<BookingAdminDetail>(
        `/v1/admin/bookings/${reference}/confirm`,
        {}
      ),
    onSuccess: (data) => {
      qc.setQueryData(["admin", "bookings", "detail", reference], data);
      qc.invalidateQueries({ queryKey: ["admin", "bookings"] });
    },
  });
}

export function useCancelBooking(reference: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (reason?: string) =>
      apiPost<BookingAdminDetail>(
        `/v1/admin/bookings/${reference}/cancel`,
        { reason: reason ?? null }
      ),
    onSuccess: (data) => {
      qc.setQueryData(["admin", "bookings", "detail", reference], data);
      qc.invalidateQueries({ queryKey: ["admin", "bookings"] });
    },
  });
}

export function useRefundBooking(reference: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (amountCents?: number) =>
      apiPost<BookingAdminDetail>(
        `/v1/admin/bookings/${reference}/refund`,
        { amountCents: amountCents ?? null }
      ),
    onSuccess: (data) => {
      qc.setQueryData(["admin", "bookings", "detail", reference], data);
      qc.invalidateQueries({ queryKey: ["admin", "bookings"] });
    },
  });
}