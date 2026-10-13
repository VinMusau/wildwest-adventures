import Logo from "./Logo";

const columns: Array<[string, ...string[]]> = [
  ["Explore", "Journeys", "Destinations", "Travel styles", "Inspiration"],
  ["About", "Our story", "Our guides", "Our impact", "Careers"],
  ["Support", "Contact us", "FAQs", "Travel insurance", "Booking conditions"],
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
            <button>ig</button>
            <button>p</button>
            <button>f</button>
          </div>
        </div>
        {columns.map(([head, ...items]) => (
          <div className="footer-col" key={head}>
            <strong>{head}</strong>
            {items.map((x) => (
              <button key={x}>{x}</button>
            ))}
          </div>
        ))}
      </div>
      <div className="footer-bottom">
        <span>© {new Date().getFullYear()} Wild West Adventures</span>
        <span>Privacy · Cookies · Terms</span>
        <span>ABTA · ATOL PROTECTED</span>
      </div>
    </footer>
  );
}