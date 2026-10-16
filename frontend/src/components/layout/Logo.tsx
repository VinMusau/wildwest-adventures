import { Link } from "react-router";

interface LogoProps {
  light?: boolean;
}

export default function Logo({ light = false }: LogoProps) {
  return (
    <Link
      to="/"
      className={`logo ${light ? "light" : ""}`}
      aria-label="Wild West Adventures home"
    >
      <img
        src={light ? "/logo-light.png" : "/logo.png"}
        alt="Wild West Adventures"
        className="logo-img"
      />
    </Link>
  );
}