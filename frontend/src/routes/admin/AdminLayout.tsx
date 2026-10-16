import { NavLink, Outlet } from "react-router";
import Logo from "@/components/layout/Logo";
import { useAuth } from "@/features/auth/AuthContext";

export default function AdminLayout() {
  const { user, logout } = useAuth();

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `admin-nav-link ${isActive ? "active" : ""}`;

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-sidebar-head">
          <Logo light />
          <span className="admin-tag">Admin</span>
        </div>

        <nav className="admin-nav">
          <NavLink to="/admin" end className={linkClass}>Dashboard</NavLink>
          <NavLink to="/admin/tours" className={linkClass}>Tours</NavLink>
          <NavLink to="/admin/bookings" className={linkClass}>Bookings</NavLink>
          <NavLink to="/admin/customers" className={linkClass}>Customers</NavLink>
        </nav>

        <div className="admin-sidebar-foot">
          <small>
            Signed in as<br />
            <strong>{user?.email}</strong>
          </small>
          <button className="nav-link-btn" onClick={logout}>Sign out</button>
        </div>
      </aside>

      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}