import { useState } from "react";
import { Link } from "react-router";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import Logo from "./Logo";

interface NavProps {
  light?: boolean;
}

export default function Nav({ light = false }: NavProps) {
  const [open, setOpen] = useState(false);

  return (
    <header className={`nav ${light ? "nav-light" : ""}`}>
      <Logo light={light} />
      <nav className={`nav-links ${open ? "open" : ""}`}>
        <Link to="/journeys" onClick={() => setOpen(false)}>
          Journeys
        </Link>
        <Link to="/destinations" onClick={() => setOpen(false)}>
          Destinations
        </Link>
        <Link to="/styles" onClick={() => setOpen(false)}>
          Travel styles
        </Link>
        <Link to="/story" onClick={() => setOpen(false)}>
          Our story
        </Link>
        <Link
          to="/journeys"
          className={`button ${light ? "secondary" : "primary"}`}
          onClick={() => setOpen(false)}
        >
          Plan your journey
        </Link>
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