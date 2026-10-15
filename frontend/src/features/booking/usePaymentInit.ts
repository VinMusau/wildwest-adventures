import { useMutation } from "@tanstack/react-query";
import { apiPost } from "@/lib/api";
import type { PaymentInitResponse } from "@/types";

export function usePaymentInit() {
  return useMutation({
    mutationFn: (reference: string) =>
      apiPost<PaymentInitResponse>(
        `/v1/bookings/${reference}/payment-init`,
        {}
      ),
  });
}