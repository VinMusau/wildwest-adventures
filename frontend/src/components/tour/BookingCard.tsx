import { useState } from "react";
import { Link } from "react-router";
import Icon from "@/components/ui/Icon";

interface BookingCardProps {
  priceFrom: string;
  bookHref: string;
}

export default function BookingCard({ priceFrom, bookHref }: BookingCardProps) {
  const [guests, setGuests] = useState(2);
  const [date, setDate] = useState("");

  // Stepper logic (min 1, max 8)
  const handleMinus = () => setGuests((g) => Math.max(1, g - 1));
  const handlePlus = () => setGuests((g) => Math.min(8, g + 1));

  // Build the URL with query parameters
  const params = new URLSearchParams();
  if (date) params.set("date", date);
  params.set("guests", guests.toString());
  const finalHref = `${bookHref}?${params.toString()}`;

  // Get today's date in YYYY-MM-DD format for the "min" attribute
  const today = new Date().toISOString().split("T")[0];

  return (
    <aside className="booking-card">
      <span>From</span>
      <h3>
        {priceFrom} <small>per person</small>
      </h3>
      <hr />
      
      <label>
        Preferred departure
        <input 
          type="date" 
          value={date}
          min={today}
          onChange={(e) => setDate(e.target.value)}
          style={{ cursor: "pointer" }}
        />
      </label>
      
      <label>
        Travellers
        <div className="stepper">
          <button 
            type="button" 
            onClick={handleMinus} 
            disabled={guests <= 1}
          >
            −
          </button>
          <span>{guests} {guests === 1 ? "guest" : "guests"}</span>
          <button 
            type="button" 
            onClick={handlePlus} 
            disabled={guests >= 8}
          >
            +
          </button>
        </div>
      </label>

      <Link to={finalHref} className="button primary">
        Check availability <Icon name="arrow" />
      </Link>

      <p>
        <Icon name="shield" /> No payment required today
      </p>
      <hr />
      <strong>Need a little help?  </strong>
      <small>
        Speak to a destination expert
        <br />
        +254 797 779 935
      </small>
    </aside>
  );
}