import Skeleton from "@/components/ui/Skeleton";

export default function AvailabilitySkeleton() {
  return (
    <div className="departure-list">
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="departure-card" style={{ pointerEvents: "none" }}>
          <Skeleton style={{ width: "70%", height: 14, marginBottom: 6 }} />
          <Skeleton style={{ width: "50%", height: 10, marginBottom: 10 }} />
          <Skeleton style={{ width: "40%", height: 12 }} />
        </div>
      ))}
    </div>
  );
}