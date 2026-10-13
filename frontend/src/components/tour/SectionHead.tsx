import { Link } from "react-router";
import Icon from "@/components/ui/Icon";

interface SectionHeadProps {
  eyebrow: string;
  title: string;
  copy?: string;
  action?: string;
  actionTo?: string;
}

export default function SectionHead({
  eyebrow,
  title,
  copy,
  action,
  actionTo = "/journeys",
}: SectionHeadProps) {
  return (
    <div className="section-head">
      <div>
        <span className="eyebrow">{eyebrow}</span>
        <h2>{title}</h2>
        {copy && <p>{copy}</p>}
      </div>
      {action && (
        <Link to={actionTo} className="text-link">
          {action}
          <Icon name="arrow" />
        </Link>
      )}
    </div>
  );
}