import { useMutation } from "@tanstack/react-query";
import { apiPost } from "@/lib/api";
import type { PaymentIntentResponse } from "@/types";

export function usePaymentIntent() {
  return useMutation({
    mutationFn: (reference: string) =>
      apiPost<PaymentIntentResponse>(
        `/v1/bookings/${reference}/payment-intent`,
        {}
      ),
  });
}