import { Link } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import { useMyBookings } from "@/features/booking/useBooking";
import { formatPrice } from "@/lib/format";
import type { BookingStatus } from "@/types";
import { useQueryClient } from "@tanstack/react-query";
import { apiPost } from "@/lib/api";

const STATUS_LABEL: Record<BookingStatus, string> = {
  PENDING: "Pending",
  CONFIRMED: "Confirmed",
  CANCELLED: "Cancelled",
  EXPIRED: "Expired",
};

export default function MyBookingsPage() {
  const { data: bookings = [], isLoading } = useMyBookings();

  const queryClient = useQueryClient();

  return (
    <main className="light-page">
      <Nav />
      <div className="section" style={{ paddingTop: 80 }}>
        <span className="eyebrow">Your account</span>
        <h1
          style={{
            fontFamily: "var(--serif)",
            fontSize: 56,
            margin: "8px 0 40px",
          }}
        >
          My bookings
        </h1>

        {isLoading ? (
          <p className="text-muted">Loading your bookings…</p>
        ) : bookings.length === 0 ? (
          <div>
            <p className="text-muted">You don't have any bookings yet.</p>
            <Link
              to="/journeys"
              className="button primary"
              style={{ marginTop: 16 }}
            >
              Browse journeys
            </Link>
          </div>
        ) : (
          <div className="bookings-list">
            {bookings.map((b) => (
              <article className="booking-row" key={b.id}>
                <img src={b.tourImageUrl} alt={b.tourTitle} />
                <div className="booking-row-body">
                  <div className="eyebrow-row">
                    <span>{b.reference}</span>
                    <span
                      className={`status-badge status-${b.status.toLowerCase()}`}
                    >
                      {STATUS_LABEL[b.status]}
                    </span>
                  </div>
                  <h3>{b.tourTitle}</h3>
                  <p className="text-muted">
                    {b.tourPlace} ·{" "}
                    {new Date(b.departureDate).toLocaleDateString("en-US", {
                      day: "numeric",
                      month: "long",
                      year: "numeric",
                    })}{" "}
                    · {b.travelersCount}{" "}
                    {b.travelersCount === 1 ? "traveller" : "travellers"}
                  </p>
                </div>

                <div className="booking-row-price">
                  <span>{formatPrice(b.totalCents, b.currency)}</span>
                  {b.status === "PENDING" && (
                    <button
                      className="text-link"
                      onClick={async () => {
                        try {
                          await apiPost(
                            `/v1/bookings/${b.reference}/verify-payment`,
                            {},
                          );
                          queryClient.invalidateQueries({
                            queryKey: ["bookings", "mine"],
                          });
                        } catch (e) {
                          console.error(e);
                        }
                      }}
                    >
                      Verify payment
                    </button>
                  )}
                  <Link to={`/confirmed/${b.reference}`} className="text-link">
                    View <IconArrow />
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
      <Footer />
    </main>
  );
}

function IconArrow() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}