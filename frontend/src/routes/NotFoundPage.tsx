import { Link } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";

export default function NotFoundPage() {
  return (
    <main className="light-page">
      <Nav />
      <div className="not-found-shell">
        <span className="eyebrow">404 — Page not found</span>
        <h1>You've wandered off the map.</h1>
        <p>
          The page you are looking for doesn't exist, has been moved, or is on a
          journey of its own.
        </p>
        <div className="not-found-actions">
          <Link to="/" className="button primary">
            Return home <Icon name="arrow" />
          </Link>
          <Link to="/journeys" className="button secondary">
            Browse journeys
          </Link>
        </div>
      </div>
      <Footer />
    </main>
  );
}