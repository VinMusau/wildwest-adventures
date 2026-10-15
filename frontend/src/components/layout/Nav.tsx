import { useState } from "react";
import { Link } from "react-router";
import Icon from "@/components/ui/Icon";
import { useAuth } from "@/features/auth/AuthContext";
import Logo from "./Logo";

interface NavProps {
  light?: boolean;
}

export default function Nav({ light = false }: NavProps) {
  const [open, setOpen] = useState(false);
  const { user, logout } = useAuth();

  const close = () => setOpen(false);

  return (
    <header className={`nav ${light ? "nav-light" : ""}`}>
      <Logo light={light} />
      <nav className={`nav-links ${open ? "open" : ""}`}>
        <Link to="/journeys" onClick={close}>Journeys</Link>
        <Link to="/destinations" onClick={close}>Destinations</Link>
        <Link to="/styles" onClick={close}>Travel styles</Link>
        <Link to="/story" onClick={close}>Our story</Link>

        {user ? (
          <>
            <Link to="/account/bookings" onClick={close}>
              My Bookings
            </Link>
            <span className="nav-user">{user.firstName}</span>
            <button className="nav-link-btn" onClick={() => { logout(); close(); }}>
              Sign out
            </button>
          </>
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
      <button className="menu-button" onClick={() => setOpen(!open)} aria-label="Toggle menu">
        <Icon name={open ? "close" : "menu"} />
      </button>
    </header>
  );
}