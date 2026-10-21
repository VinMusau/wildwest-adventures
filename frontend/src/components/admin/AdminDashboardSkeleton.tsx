export default function AdminDashboardSkeleton() {
  return (
    <div className="admin-page">
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">Operator console</span>
          <h1>Dashboard</h1>
        </div>
      </div>

      {/* KPI Cards Skeleton */}
      <div className="admin-kpi-grid">
        {[1, 2, 3, 4].map((i) => (
          <div className="admin-kpi-card" key={i}>
            <div className="skeleton skeleton-text" style={{ width: "40%", height: 12 }} />
            <div className="skeleton skeleton-title" style={{ width: "70%", height: 32, marginTop: 12 }} />
            <div className="skeleton skeleton-text" style={{ width: "50%", height: 12, marginTop: 16 }} />
          </div>
        ))}
      </div>

      <div className="admin-dashboard-grid">
        {/* Chart Skeleton */}
        <section className="admin-form-section">
          <div className="skeleton skeleton-title" style={{ width: "30%", height: 24, marginBottom: 24 }} />
          <div className="skeleton skeleton-chart" style={{ width: "100%", height: 280 }} />
        </section>

        {/* Recent Bookings Skeleton */}
        <section className="admin-form-section">
          <div className="skeleton skeleton-title" style={{ width: "40%", height: 24, marginBottom: 24 }} />
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            {[1, 2, 3, 4].map((i) => (
              <div key={i} style={{ display: "flex", justifyContent: "space-between" }}>
                <div>
                  <div className="skeleton skeleton-text" style={{ width: 100, height: 14, marginBottom: 8 }} />
                  <div className="skeleton skeleton-text" style={{ width: 150, height: 12 }} />
                </div>
                <div className="skeleton skeleton-text" style={{ width: 60, height: 24, borderRadius: 999 }} />
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}