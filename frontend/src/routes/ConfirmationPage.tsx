import { Link, useParams } from "react-router";
import Nav from "@/components/layout/Nav";
import Icon from "@/components/ui/Icon";

export default function ConfirmationPage() {
  const { reference } = useParams<{ reference: string }>();

  return (
    <main className="confirmed">
      <Nav />
      <div>
        <span className="success">
          <Icon name="check" size={30} />
        </span>
        <span className="eyebrow">Booking confirmed</span>
        <h1>
          Your wild story
          <br />
          starts here.
        </h1>
        <p>
          Thank you, Alex. Your journey specialist will be in touch within 24
          hours to take care of every detail.
        </p>
        <div className="confirmation-card">
          <span>BOOKING {reference ?? "RW-00000"}</span>
          <strong>The Great Migration, Privately</strong>
          <p>12 September 2026 · 2 travellers</p>
        </div>
        <Link to="/" className="button primary">
          Return home
        </Link>
      </div>
    </main>
  );
}