import { Link } from "react-router";
import Logo from "./Logo";
import { useToday } from "@/lib/useToday";

const columns = [
  {
    head: "Explore",
    links: [
      { label: "Journeys", to: "/journeys" },
      { label: "Destinations", to: "/destinations" },
      { label: "Travel styles", to: "/styles" },
    ],
  },
  {
    head: "Company",
    links: [
      { label: "Our story", to: "/story" },
      { label: "Contact us", to: "mailto:hello@wildwestadventures.com" },
    ],
  },
  {
    head: "Legal",
    links: [
      { label: "Privacy Policy", to: "/privacy" },
      { label: "Terms of Service", to: "/terms" },
      { label: "Cookie Policy", to: "/cookies" },
    ],
  },
];

// Icons: white PNGs rendered from Simple Icons (CC0), in public/icons.
const socials = [
  {
    label: "Instagram",
    href: "https://instagram.com",
    icon: "/icons/instagram.png",
  },
  {
    label: "TikTok",
    href: "https://tiktok.com/@wildwestadventures",
    icon: "/icons/tiktok.png",
  },
  {
    label: "Facebook",
    href: "https://facebook.com",
    icon: "/icons/facebook.png",
  },
];

export default function Footer() {
  const year = useToday().slice(0, 4);
  return (
    <footer>
      <div className="footer-top">
        <div>
          <Logo light />
          <p>
            Journeys into the world's wildest places, designed with care and
            travelled with purpose.
          </p>
          <div className="socials">
            {socials.map((s) => (
              <a
                key={s.label}
                href={s.href}
                target="_blank"
                rel="noopener noreferrer"
                aria-label={s.label}
              >
                <img src={s.icon} alt="" width={18} height={18} />
              </a>
            ))}
          </div>
        </div>

        {columns.map((col) => (
          <div className="footer-col" key={col.head}>
            <strong>{col.head}</strong>
            {col.links.map((link) => (
              <Link key={link.label} to={link.to}>
                {link.label}
              </Link>
            ))}
          </div>
        ))}
      </div>

      <div className="footer-bottom">
        <span>© {year} Wild West Adventures</span>
        <span>
          <Link to="/privacy">Privacy</Link> ·{" "}
          <Link to="/cookies">Cookies</Link> · <Link to="/terms">Terms</Link>
        </span>
        <span>ABTA · ATOL PROTECTED</span>
      </div>
    </footer>
  );
}
