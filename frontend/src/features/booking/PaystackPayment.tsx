import { useState } from "react";
import { loadPaystack } from "@alexasomba/paystack-inline";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";

interface PaystackPaymentProps {
  accessCode: string;
  amountLabel: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function PaystackPayment({
  accessCode,
  amountLabel,
  onSuccess,
  onCancel,
}: PaystackPaymentProps) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const openPopup = async () => {
    setBusy(true);
    setError(null);
    try {
      const PaystackPop = await loadPaystack();
      const popup = new PaystackPop();
      popup.resumeTransaction(accessCode, {
        onSuccess: () => {
          // Paystack calls this on the client. We still need the server-side
          // webhook to flip the booking — this just closes the loop for the user.
          onSuccess();
        },
        onCancel: () => {
          setBusy(false);
          onCancel();
        },
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not open payment");
      setBusy(false);
    }
  };

  return (
    <div>
      <div
        className="payment"
        style={{ background: "#fff9f5", borderColor: "var(--terracotta)" }}
      >
        <strong>Pay with Paystack</strong>
        <p style={{ fontSize: 13, color: "var(--muted)" }}>
          Card, M-Pesa, Airtel Money, or bank transfer — all handled securely
          by Paystack.
        </p>
      </div>

      {error && (
        <p className="form-error" style={{ marginTop: 16 }}>
          {error}
        </p>
      )}

      <div className="form-actions" style={{ marginTop: 24 }}>
        <Button onClick={openPopup} disabled={busy}>
          {busy ? "Opening…" : `Pay ${amountLabel}`} <Icon name="arrow" />
        </Button>
      </div>
    </div>
  );
}