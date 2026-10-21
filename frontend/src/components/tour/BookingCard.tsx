import { Link } from "react-router";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";

interface BookingCardProps {
  priceFrom: string;
  bookHref: string;
}

export default function BookingCard({ priceFrom, bookHref }: BookingCardProps) {
  return (
    <aside className="booking-card">
      <span>From</span>
      <h3>
        {priceFrom} <small>per person</small>
      </h3>
      <hr />
      <label>
        Preferred departure
        <input type="text" placeholder="Select a date" />
      </label>
      <label>
        Travellers
        <div className="stepper">
          <button type="button">−</button>
          <span>2 guests</span>
          <button type="button">+</button>
        </div>
      </label>
      <Link to={bookHref} className="button primary">
        Check availability <Icon name="arrow" />
      </Link>
      <p>
        <Icon name="shield" /> No payment required today
      </p>
      <hr />
      <strong>Need a little help?   </strong>
      <small>
        Speak to a destination expert 
        <br />
        +254 797 779 935
      </small>
    </aside>
  );
}