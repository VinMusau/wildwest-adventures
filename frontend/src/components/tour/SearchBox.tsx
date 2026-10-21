import { Link } from "react-router";
import Icon from "@/components/ui/Icon";

export default function SearchBox() {
  return (
    <div className="search-box">
      <label>
        <span>Where to?</span>
        <div>
          <Icon name="pin" />
          <input defaultValue="East Africa" aria-label="Destination" />
        </div>
      </label>
      <label>
        <span>When?</span>
        <div>
          <Icon name="calendar" />
          <input placeholder="Choose dates" aria-label="Dates" type="date" />
        </div>
      </label>
      <label>
        <span>Travellers</span>
        <div>
          <Icon name="users" />
          <input defaultValue="2 guests" aria-label="Travellers" />
        </div>
      </label>
      <Link to="/journeys" className="button primary">
        <Icon name="search" /> Find journeys
      </Link>
    </div>
  );
}