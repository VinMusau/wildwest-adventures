import { Link } from "react-router";
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useAdminDashboard } from "@/features/admin/useAdminDashboard";
import { formatPrice } from "@/lib/format";
import type { BookingStatus } from "@/types";

const STATUS_LABEL: Record<BookingStatus, string> = {
  PENDING: "Pending",
  CONFIRMED: "Confirmed",
  CANCELLED: "Cancelled",
  EXPIRED: "Expired",
};

export default function AdminDashboardPage() {
  const { data, isLoading } = useAdminDashboard();

  if (isLoading || !data) {
    return (
      <div className="admin-page">
        <span className="eyebrow">Operator console</span>
        <h1>Dashboard</h1>
        <p className="text-muted">Loading…</p>
      </div>
    );
  }

  const { kpis, revenueSeries, topTours, recentBookings } = data;

  const bookingsDelta = pctDelta(
    kpis.bookingsThisMonth,
    kpis.bookingsLastMonth
  );
  const revenueDelta = pctDelta(
    kpis.revenueThisMonthCents,
    kpis.revenueLastMonthCents
  );

  const chartData = revenueSeries.map((p) => ({
    date: p.date,
    revenue: p.revenueCents / 100,
    bookings: p.bookingsCount,
  }));

  const conversion =
    kpis.totalBookings > 0
      ? ((kpis.confirmedTotal / kpis.totalBookings) * 100).toFixed(1)
      : "0.0";

  return (
    <div className="admin-page">
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">Operator console</span>
          <h1>Dashboard</h1>
        </div>
      </div>

      <div className="admin-kpi-grid">
        <KpiCard
          label="Bookings this month"
          value={kpis.bookingsThisMonth.toString()}
          delta={bookingsDelta}
        />
        <KpiCard
          label="Revenue this month"
          value={formatPrice(kpis.revenueThisMonthCents, "USD")}
          delta={revenueDelta}
        />
        <KpiCard label="Confirmed (all time)" value={kpis.confirmedTotal.toString()} />
        <KpiCard label="Conversion rate" value={`${conversion}%`} />
      </div>

      <section className="admin-form-section">
        <h2>Revenue — last 90 days</h2>
        {chartData.length === 0 ? (
          <p className="text-muted">No paid bookings yet.</p>
        ) : (
          <div style={{ width: "100%", height: 280 }}>
            <ResponsiveContainer>
              <LineChart data={chartData} margin={{ top: 8, right: 16, bottom: 8, left: 0 }}>
                <CartesianGrid stroke="#e8e1d6" vertical={false} />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 11, fill: "#6b6560" }}
                  tickFormatter={(v) =>
                    new Date(v).toLocaleDateString("en-US", {
                      month: "short",
                      day: "numeric",
                    })
                  }
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#6b6560" }}
                  tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`}
                />
                <Tooltip
                  contentStyle={{
                    background: "white",
                    border: "1px solid #e8e1d6",
                    borderRadius: 8,
                    fontSize: 12,
                  }}
                  formatter={(value: number) => [
                    `$${value.toLocaleString()}`,
                    "Revenue",
                  ]}
                  labelFormatter={(v) =>
                    new Date(v).toLocaleDateString("en-US", {
                      weekday: "short",
                      day: "numeric",
                      month: "short",
                    })
                  }
                />
                <Line
                  type="monotone"
                  dataKey="revenue"
                  stroke="#b4552d"
                  strokeWidth={2}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>

      <div className="admin-dash-split">
        <section className="admin-form-section">
          <h2>Top tours</h2>
          {topTours.length === 0 ? (
            <p className="text-muted">No bookings yet.</p>
          ) : (
            <ul className="admin-list">
              {topTours.map((t) => (
                <li key={t.slug}>
                  <Link to={`/admin/tours/${t.slug}/edit`} className="admin-link">
                    {t.title}
                  </Link>
                  <small>
                    {t.bookingsCount} bookings · {formatPrice(t.revenueCents, "USD")}
                  </small>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="admin-form-section">
          <div className="admin-section-head">
            <h2>Recent bookings</h2>
            <Link to="/admin/bookings" className="text-link" style={{ fontSize: 12 }}>
              View all
            </Link>
          </div>
          {recentBookings.length === 0 ? (
            <p className="text-muted">No bookings yet.</p>
          ) : (
            <ul className="admin-list">
              {recentBookings.map((b) => (
                <li key={b.reference}>
                  <div>
                    <span className="admin-mono">{b.reference}</span>
                    <small>{b.customerName} · {b.tourTitle}</small>
                  </div>
                  <span className={`status-badge status-${b.status.toLowerCase()}`}>
                    {STATUS_LABEL[b.status]}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}

function KpiCard({
  label,
  value,
  delta,
}: {
  label: string;
  value: string;
  delta?: number | null;
}) {
  return (
    <div className="admin-kpi-card">
      <span>{label}</span>
      <strong>{value}</strong>
      {delta != null && (
        <small className={delta >= 0 ? "delta-up" : "delta-down"}>
          {delta >= 0 ? "▲" : "▼"} {Math.abs(delta).toFixed(0)}% vs last month
        </small>
      )}
    </div>
  );
}

function pctDelta(current: number, previous: number): number | null {
  if (previous === 0) return current === 0 ? 0 : null;
  return ((current - previous) / previous) * 100;
}