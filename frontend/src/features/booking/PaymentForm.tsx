import { useState } from "react";
import {
  PaymentElement,
  useElements,
  useStripe,
} from "@stripe/react-stripe-js";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";

interface PaymentFormProps {
  onSuccess: () => void;
  amountLabel: string;
}

export default function PaymentForm({ onSuccess, amountLabel }: PaymentFormProps) {
  const stripe = useStripe();
  const elements = useElements();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!stripe || !elements) return;

    setBusy(true);
    setError(null);

    const { error: submitError } = await elements.submit();
    if (submitError) {
      setError(submitError.message ?? "Payment could not be submitted");
      setBusy(false);
      return;
    }

    const { error: confirmError } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/payment/return`,
      },
      redirect: "if_required",
    });

    if (confirmError) {
      setError(confirmError.message ?? "Payment failed");
      setBusy(false);
      return;
    }

    // No redirect required — card succeeded inline.
    onSuccess();
  };

  return (
    <form onSubmit={submit}>
      <PaymentElement />

      {error && (
        <p className="form-error" style={{ marginTop: 16 }}>
          {error}
        </p>
      )}

      <div className="form-actions" style={{ marginTop: 24 }}>
        <Button type="submit" disabled={!stripe || busy}>
          {busy ? "Processing…" : `Pay ${amountLabel}`} <Icon name="arrow" />
        </Button>
      </div>
    </form>
  );
}