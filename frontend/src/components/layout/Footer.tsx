import { Link } from "react-router";
import Logo from "./Logo";

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

export default function Footer() {
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
            <a
              href="https://instagram.com"
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Instagram"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect>
                <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path>
                <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line>
              </svg>
            </a>
            
            {/* Replaced Pinterest with TikTok */}
            <a
              href="https://tiktok.com/@wildwestadventures" 
              target="_blank"
              rel="noopener noreferrer"
              aria-label="TikTok"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 12a4 4 0 1 0 4 4V4a5 5 0 0 0 5 5v3a3 3 0 0 1-3-3v11a4 4 0 1 1-6-4z"></path>
              </svg>
            </a>

            <a
              href="https://facebook.com"
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Facebook"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path>
              </svg>
            </a>
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
        <span>© {new Date().getFullYear()} Wild West Adventures</span>
        <span>
          <Link to="/privacy">Privacy</Link> · <Link to="/cookies">Cookies</Link> ·{" "}
          <Link to="/terms">Terms</Link>
        </span>
        <span>ABTA · ATOL PROTECTED</span>
      </div>
    </footer>
  );
}