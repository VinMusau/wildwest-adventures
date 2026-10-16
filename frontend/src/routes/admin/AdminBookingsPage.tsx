import { useState } from "react";
import Icon from "@/components/ui/Icon";
import Button from "@/components/ui/Button";
import {
  useAdminBookings,
  useAdminBooking,
  useCancelBooking,
  useConfirmBooking,
  useRefundBooking,
} from "@/features/admin/useAdminBookings";
import { useTours } from "@/features/tours/useTours";
import { formatPrice } from "@/lib/format";
import type {
  AdminBookingFilters,
  BookingAdminDetail,
  BookingStatus,
} from "@/types";

const STATUS_OPTIONS: Array<{ value: "" | BookingStatus; label: string }> = [
  { value: "", label: "All statuses" },
  { value: "PENDING", label: "Pending" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "EXPIRED", label: "Expired" },
];

const STATUS_LABEL: Record<BookingStatus, string> = {
  PENDING: "Pending",
  CONFIRMED: "Confirmed",
  CANCELLED: "Cancelled",
  EXPIRED: "Expired",
};

export default function AdminBookingsPage() {
  const [filters, setFilters] = useState<AdminBookingFilters>({});
  const [openRef, setOpenRef] = useState<string | null>(null);

  const { data, isLoading } = useAdminBookings(filters);
  const { data: tours = [] } = useTours();

  const bookings = data?.content ?? [];
  const total = data?.totalElements ?? 0;

  return (
    <div className="admin-page">
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">Operations</span>
          <h1>Bookings</h1>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            {total} {total === 1 ? "booking" : "bookings"}
          </p>
        </div>
      </div>

      <div className="admin-filter-bar">
        <select
          value={filters.status ?? ""}
          onChange={(e) =>
            setFilters({
              ...filters,
              status: (e.target.value || undefined) as BookingStatus | undefined,
              page: 0,
            })
          }
        >
          {STATUS_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>

        <select
          value={filters.tour ?? ""}
          onChange={(e) =>
            setFilters({ ...filters, tour: e.target.value || undefined, page: 0 })
          }
        >
          <option value="">All tours</option>
          {tours.map((t) => (
            <option key={t.slug} value={t.slug}>
              {t.title}
            </option>
          ))}
        </select>

        <input
          type="search"
          placeholder="Search reference, email, name…"
          value={filters.q ?? ""}
          onChange={(e) =>
            setFilters({ ...filters, q: e.target.value || undefined, page: 0 })
          }
        />
      </div>

      {isLoading ? (
        <p className="text-muted">Loading bookings…</p>
      ) : bookings.length === 0 ? (
        <p className="text-muted">No bookings match those filters.</p>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Customer</th>
                <th>Tour</th>
                <th>Departure</th>
                <th>Guests</th>
                <th>Total</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr
                  key={b.id}
                  className="admin-row-clickable"
                  onClick={() => setOpenRef(b.reference)}
                >
                  <td>
                    <span className="admin-mono">{b.reference}</span>
                  </td>
                  <td>
                    {b.customerName}
                    <small className="admin-slug">{b.customerEmail}</small>
                  </td>
                  <td>{b.tourTitle}</td>
                  <td>
                    {new Date(b.departureDate).toLocaleDateString("en-US", {
                      day: "numeric",
                      month: "short",
                      year: "numeric",
                    })}
                  </td>
                  <td>{b.travelersCount}</td>
                  <td>{formatPrice(b.totalCents, b.currency)}</td>
                  <td>
                    <span className={`status-badge status-${b.status.toLowerCase()}`}>
                      {STATUS_LABEL[b.status]}
                    </span>
                  </td>
                  <td className="admin-cell-actions">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setOpenRef(b.reference);
                      }}
                    >
                      Manage
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="admin-pagination">
          <Button
            variant="ghost"
            disabled={(filters.page ?? 0) === 0}
            onClick={() =>
              setFilters({ ...filters, page: (filters.page ?? 0) - 1 })
            }
          >
            Previous
          </Button>
          <span>
            Page {(filters.page ?? 0) + 1} of {data.totalPages}
          </span>
          <Button
            variant="ghost"
            disabled={(filters.page ?? 0) >= data.totalPages - 1}
            onClick={() =>
              setFilters({ ...filters, page: (filters.page ?? 0) + 1 })
            }
          >
            Next
          </Button>
        </div>
      )}

      {openRef && (
        <BookingDetailDrawer
          reference={openRef}
          onClose={() => setOpenRef(null)}
        />
      )}
    </div>
  );
}

// ---------- Drawer ----------

function BookingDetailDrawer({
  reference,
  onClose,
}: {
  reference: string;
  onClose: () => void;
}) {
  const { data: booking, isLoading } = useAdminBooking(reference);
  const confirm = useConfirmBooking(reference);
  const cancel = useCancelBooking(reference);
  const refund = useRefundBooking(reference);

  const anyBusy = confirm.isPending || cancel.isPending || refund.isPending;

  return (
    <div className="admin-drawer-backdrop" onClick={onClose}>
      <aside className="admin-drawer" onClick={(e) => e.stopPropagation()}>
        <div className="admin-drawer-head">
          <div>
            <span className="eyebrow">Booking</span>
            <h2>{reference}</h2>
          </div>
          <button
            className="admin-drawer-close"
            onClick={onClose}
            aria-label="Close"
          >
            <Icon name="close" />
          </button>
        </div>

        {isLoading || !booking ? (
          <p className="text-muted">Loading…</p>
        ) : (
          <div className="admin-drawer-body">
            <BookingSummaryBlock booking={booking} />

            <section className="admin-drawer-section">
              <h3>Lead traveller</h3>
              <p>
                <strong>
                  {booking.leadFirstName} {booking.leadLastName}
                </strong>
                <br />
                {booking.leadEmail}
                {booking.leadPhone && (
                  <>
                    <br />
                    {booking.leadPhone}
                  </>
                )}
                {booking.leadCountry && (
                  <>
                    <br />
                    {booking.leadCountry}
                  </>
                )}
              </p>
            </section>

            <section className="admin-drawer-section">
              <h3>Travellers ({booking.travelers.length})</h3>
              <ul className="admin-list">
                {booking.travelers.map((t) => (
                  <li key={t.id}>
                    {t.firstName} {t.lastName}
                    <small>{t.travelerType}</small>
                  </li>
                ))}
              </ul>
            </section>

            {booking.addons.length > 0 && (
              <section className="admin-drawer-section">
                <h3>Add-ons</h3>
                <ul className="admin-list">
                  {booking.addons.map((a) => (
                    <li key={a.id}>
                      {a.label}
                      <small>
                        {formatPrice(a.priceCents, booking.currency)}
                      </small>
                    </li>
                  ))}
                </ul>
              </section>
            )}

            <section className="admin-drawer-section">
              <h3>Payment</h3>
              <dl className="admin-kv">
                <dt>Provider</dt>
                <dd>{booking.paymentProvider ?? "—"}</dd>
                <dt>Paid at</dt>
                <dd>
                  {booking.paidAt
                    ? new Date(booking.paidAt).toLocaleString()
                    : "—"}
                </dd>
                <dt>Paystack ref</dt>
                <dd className="admin-mono">
                  {booking.paystackReference ?? "—"}
                </dd>
                {booking.refundedAt && (
                  <>
                    <dt>Refunded at</dt>
                    <dd>{new Date(booking.refundedAt).toLocaleString()}</dd>
                    <dt>Refund ref</dt>
                    <dd className="admin-mono">
                      {booking.refundReference ?? "—"}
                    </dd>
                    <dt>Refund amount</dt>
                    <dd>
                      {booking.refundAmountCents != null
                        ? formatPrice(
                            booking.refundAmountCents,
                            booking.currency
                          )
                        : "—"}
                    </dd>
                  </>
                )}
              </dl>
            </section>

            {booking.status === "PENDING" && booking.expiresAt && (
              <p className="admin-note">
                Hold expires {new Date(booking.expiresAt).toLocaleString()}
              </p>
            )}

            <section className="admin-drawer-section">
              <h3>Actions</h3>
              <div className="admin-actions">
                {booking.status === "PENDING" && (
                  <Button
                    onClick={() => confirm.mutate()}
                    disabled={anyBusy}
                  >
                    Confirm booking
                  </Button>
                )}

                {(booking.status === "PENDING" ||
                  booking.status === "CONFIRMED") && (
                  <Button
                    variant="secondary"
                    disabled={anyBusy}
                    onClick={() => {
                      const reason = window.prompt(
                        "Reason for cancellation (optional):"
                      );
                      if (reason === null) return; // user cancelled the prompt
                      cancel.mutate(reason.trim() || undefined);
                    }}
                  >
                    Cancel booking
                  </Button>
                )}

                {booking.paidAt && !booking.refundedAt && (
                  <Button
                    variant="secondary"
                    disabled={anyBusy}
                    onClick={() => {
                      const confirmed = window.confirm(
                        `Refund ${formatPrice(
                          booking.totalCents,
                          booking.currency
                        )} via Paystack? This releases the departure inventory and marks the booking cancelled.`
                      );
                      if (!confirmed) return;
                      refund.mutate();
                    }}
                  >
                    Refund payment
                  </Button>
                )}
              </div>

              {(confirm.isError || cancel.isError || refund.isError) && (
                <p className="form-error" style={{ marginTop: 12 }}>
                  Action failed. Check the backend logs for details.
                </p>
              )}
            </section>
          </div>
        )}
      </aside>
    </div>
  );
}

function BookingSummaryBlock({ booking }: { booking: BookingAdminDetail }) {
  return (
    <div className="admin-drawer-summary">
      <img src={booking.tourImageUrl} alt={booking.tourTitle} />
      <div>
        <span className="eyebrow">{booking.tourPlace}</span>
        <h3>{booking.tourTitle}</h3>
        <p>
          {new Date(booking.departureDate).toLocaleDateString("en-US", {
            day: "numeric",
            month: "long",
            year: "numeric",
          })}{" "}
          · {booking.travelersCount}{" "}
          {booking.travelersCount === 1 ? "traveller" : "travellers"}
        </p>
        <strong>{formatPrice(booking.totalCents, booking.currency)}</strong>
        <span className={`status-badge status-${booking.status.toLowerCase()}`}>
          {STATUS_LABEL[booking.status]}
        </span>
      </div>
    </div>
  );
}