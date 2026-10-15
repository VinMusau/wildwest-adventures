import { useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import Nav from "@/components/layout/Nav";

export default function PaymentReturnPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const paymentIntent = params.get("payment_intent");
    const status = params.get("redirect_status");

    // The webhook will confirm the booking; we just bounce the user
    // to their bookings list where they'll see the CONFIRMED status.
    if (status === "succeeded" && paymentIntent) {
      // We don't have the reference here — bounce to bookings list.
      navigate("/account/bookings", { replace: true });
    } else {
      navigate("/account/bookings", { replace: true });
    }
  }, [params, navigate]);

  return (
    <main className="light-page">
      <Nav />
      <div className="section" style={{ paddingTop: 80, textAlign: "center" }}>
        <p className="text-muted">Finalising your booking…</p>
        <p style={{ marginTop: 16 }}>
          <Link to="/account/bookings">View my bookings</Link>
        </p>
      </div>
    </main>
  );
}