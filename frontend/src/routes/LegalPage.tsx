import { Link, useLocation, useParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Footer from "@/components/layout/Footer";
import Icon from "@/components/ui/Icon";

const CONTENT = {
  privacy: {
    title: "Privacy Policy",
    updated: "October 2026",
    body: (
      <>
        <h2>1. Information We Collect</h2>
        <p>
          When you book a journey with Wild West Adventures, we collect personal
          information necessary to arrange your travel. This includes your name,
          email address, phone number, passport details (for international
          travel), and payment information.
        </p>
        <h2>2. How We Use Your Information</h2>
        <p>
          We use your information to process bookings, communicate with you about
          your trip, and comply with legal requirements. We do not sell your
          personal data to third parties.
        </p>
        <h2>3. Payment Processing</h2>
        <p>
          All payments are processed securely through our partners, including
          Paystack and Stripe. We do not store your full credit card details on
          our servers.
        </p>
        <h2>4. Your Rights</h2>
        <p>
          You have the right to request access to, correction of, or deletion of
          your personal data. To exercise these rights, please contact us at
          hello@wildwestadventures.com.
        </p>
      </>
    ),
  },
  terms: {
    title: "Terms of Service",
    updated: "October 2026",
    body: (
      <>
        <h2>1. Booking & Payments</h2>
        <p>
          A booking is only confirmed once a deposit or full payment has been
          received and cleared. Prices are quoted in USD and converted to KES at
          the time of payment. We reserve the right to adjust prices in the event
          of unforeseen currency fluctuations or supplier increases.
        </p>
        <h2>2. Cancellations & Refunds</h2>
        <p>
          Cancellation policies vary by journey. Please refer to the specific
          journey's booking conditions provided at checkout. Generally,
          cancellations made 60+ days before departure are subject to a 10%
          administrative fee, while cancellations within 30 days are
          non-refundable.
        </p>
        <h2>3. Travel Insurance</h2>
        <p>
          Comprehensive travel insurance is mandatory for all guests. It must
          cover medical expenses, evacuation, and trip cancellation.
        </p>
        <h2>4. Liability</h2>
        <p>
          Wild West Adventures acts as an agent for our suppliers (hotels,
          airlines, ground operators). We are not liable for delays, injuries, or
          losses caused by circumstances beyond our control (force majeure).
        </p>
      </>
    ),
  },
  cookies: {
    title: "Cookie Policy",
    updated: "October 2026",
    body: (
      <>
        <h2>1. What Are Cookies</h2>
        <p>
          Cookies are small text files stored on your device when you visit our
          website. They help us remember your preferences and understand how you
          interact with our site.
        </p>
        <h2>2. How We Use Cookies</h2>
        <p>
          We use essential cookies to keep you logged in and secure your booking
          sessions. We also use analytics cookies (such as Google Analytics) to
          monitor site traffic and improve your browsing experience.
        </p>
        <h2>3. Managing Cookies</h2>
        <p>
          You can control or delete cookies through your browser settings.
          However, disabling essential cookies may prevent you from logging in or
          completing a booking.
        </p>
      </>
    ),
  },
};

export default function LegalPage() {
  const location = useLocation();
  const page = location.pathname.replace("/", ""); // "privacy", "terms", etc.
  const content = CONTENT[page as keyof typeof CONTENT];

  if (!content) {
    return (
      <main className="light-page">
        <Nav />
        <div className="section" style={{ minHeight: "60vh", textAlign: "center" }}>
          <h1>Page not found</h1>
          <Link to="/" className="button primary" style={{ marginTop: 20 }}>Return home</Link>
        </div>
        <Footer />
      </main>
    );
  }

  return (
    <main className="light-page">
      <Nav />
      <div className="legal-shell">
        <Link to="/" className="legal-back">
          <Icon name="arrow" /> Back to home
        </Link>
        <span className="eyebrow">Legal</span>
        <h1>{content.title}</h1>
        <p className="legal-meta">Last updated: {content.updated}</p>
        <div className="legal-body">
          {content.body}
        </div>
      </div>
      <Footer />
    </main>
  );
}