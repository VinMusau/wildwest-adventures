import { Link } from "react-router";

interface LogoProps {
  light?: boolean;
}

export default function Logo({ light = false }: LogoProps) {
  return (
    <Link to="/" className={`logo ${light ? "light" : ""}`} aria-label="Wild West home">
      <span className="logo-mark">RW</span>
      <span>
        <strong>WILD WEST</strong>
        <small>ADVENTURES </small>
      </span>
    </Link>
  );
}