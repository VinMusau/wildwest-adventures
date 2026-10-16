import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router";
import Icon from "@/components/ui/Icon";
import { useAuth } from "@/features/auth/AuthContext";
import Logo from "./Logo";

interface NavProps {
  light?: boolean;
}

export default function Nav({ light = false }: NavProps) {
  const [open, setOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const menuRef = useRef<HTMLDivElement>(null);

  const close = () => setOpen(false);

  // Close the user dropdown when clicking outside it
  useEffect(() => {
    if (!menuOpen) return;
    const handler = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [menuOpen]);

  const initials = user
    ? `${user.firstName[0] ?? ""}${user.lastName?.[0] ?? ""}`.toUpperCase()
    : "";

  const handleLogout = () => {
    logout();
    close();
    setMenuOpen(false);
    navigate("/");
  };

  return (
    <header className={`nav ${light ? "nav-light" : ""}`}>
      <Logo light={light} />

      <nav className={`nav-links ${open ? "open" : ""}`}>
        <Link to="/journeys" onClick={close}>Journeys</Link>
        <Link to="/destinations" onClick={close}>Destinations</Link>
        <Link to="/styles" onClick={close}>Travel styles</Link>
        <Link to="/story" onClick={close}>Our story</Link>

        {user ? (
          <div className="nav-user-menu" ref={menuRef}>
            <button
              className="nav-avatar"
              onClick={() => setMenuOpen((v) => !v)}
              aria-haspopup="true"
              aria-expanded={menuOpen}
              aria-label="Account menu"
            >
              <span className="nav-avatar-initials">{initials}</span>
              <Icon name="down" size={13} />
            </button>

            {menuOpen && (
              <div className="nav-dropdown">
                <div className="nav-dropdown-head">
                  <strong>
                    {user.firstName} {user.lastName}
                  </strong>
                  <small>{user.email}</small>
                </div>
                <Link
                  to="/account/bookings"
                  onClick={() => {
                    setMenuOpen(false);
                    close();
                  }}
                >
                  My bookings
                </Link>
                {user.role === "ADMIN" && (
                  <Link
                    to="/admin"
                    onClick={() => {
                      setMenuOpen(false);
                      close();
                    }}
                  >
                    Admin console
                  </Link>
                )}
                <button className="nav-dropdown-signout" onClick={handleLogout}>
                  Sign out
                </button>
              </div>
            )}
          </div>
        ) : (
          <Link
            to="/login"
            className={`button ${light ? "secondary" : "primary"}`}
            onClick={close}
          >
            Sign in
          </Link>
        )}
      </nav>

      <button
        className="menu-button"
        onClick={() => setOpen(!open)}
        aria-label="Toggle menu"
      >
        <Icon name={open ? "close" : "menu"} />
      </button>
    </header>
  );
}