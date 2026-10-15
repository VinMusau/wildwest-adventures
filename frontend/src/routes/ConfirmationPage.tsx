import { Link, useParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";
import { useBooking } from "@/features/booking/useBooking";
import { formatPrice } from "@/lib/format";

export default function ConfirmationPage() {
  const { reference } = useParams<{ reference: string }>();
  const { data: booking, isLoading, error } = useBooking(reference);

  if (isLoading) {
    return (
      <main className="confirmed">
        <Nav />
        <div>
          <p className="text-muted">Loading your booking…</p>
        </div>
      </main>
    );
  }

  if (error || !booking) {
    return (
      <main className="confirmed">
        <Nav />
        <div>
          <span className="eyebrow">Not found</span>
          <h1>We couldn't find that booking.</h1>
          <p>Check the reference, or sign in to view your bookings.</p>
          <Link to="/account/bookings" className="button primary">
            View my bookings
          </Link>
        </div>
      </main>
    );
  }

  const isPending = booking.status === "PENDING";

  return (
    <main className="confirmed">
      <Nav />
      <div>
        <span className="success">
          <Icon name="check" size={30} />
        </span>
        <span className="eyebrow">
          {isPending ? "Reservation held" : "Booking confirmed"}
        </span>
        <h1>
          {isPending ? (
            <>
              Your place is
              <br />
              held for you.
            </>
          ) : (
            <>
              Your wild story
              <br />
              starts here.
            </>
          )}
        </h1>
        <p>
          Thank you, {booking.leadFirstName}. Your journey specialist will be in
          touch within 24 hours to take care of every detail.
        </p>

        <div className="confirmation-card">
          <span>BOOKING {booking.reference}</span>
          <strong>{booking.tourTitle}</strong>
          <p>
            {new Date(booking.departureDate).toLocaleDateString("en-US", {
              day: "numeric",
              month: "long",
              year: "numeric",
            })}{" "}
            · {booking.travelersCount}{" "}
            {booking.travelersCount === 1 ? "traveller" : "travellers"}
          </p>
          <p style={{ marginTop: 12, fontWeight: 600 }}>
            {formatPrice(booking.totalCents, booking.currency)}
          </p>
        </div>

        <div style={{ display: "flex", gap: 12, justifyContent: "center" }}>
          <Link to="/account/bookings" className="button secondary">
            View my bookings
          </Link>
          <Link to="/" className="button primary">
            Return home
          </Link>
        </div>
      </div>
    </main>
  );
}