export default function AdminDashboardPage() {
  return (
    <div className="admin-page">
      <span className="eyebrow">Operator console</span>
      <h1>Dashboard</h1>
      <p className="text-muted">
        KPIs, revenue charts, and recent activity land in Phase 4c.
      </p>

      <div className="admin-placeholder-grid">
        <div className="admin-placeholder-card">
          <strong>Bookings</strong>
          <span>Manage all reservations — Phase 4b</span>
        </div>
        <div className="admin-placeholder-card">
          <strong>Customers</strong>
          <span>Browse the customer list — Phase 4c</span>
        </div>
        <div className="admin-placeholder-card">
          <strong>Revenue</strong>
          <span>Charts and trends — Phase 4c</span>
        </div>
      </div>
    </div>
  );
}