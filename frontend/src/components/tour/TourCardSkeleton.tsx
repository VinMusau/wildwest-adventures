import Skeleton from "@/components/ui/Skeleton";

export default function TourCardSkeleton() {
  return (
    <article className="tour-card tour-card-skeleton">
      <div className="tour-image" style={{ background: "#e8e1d6" }}>
        <Skeleton style={{ width: "100%", height: "100%", borderRadius: 0 }} />
      </div>
      <div className="tour-body">
        <div className="eyebrow-row" style={{ display: "flex", justifyContent: "space-between", marginBottom: 16 }}>
          <Skeleton style={{ width: "40%", height: 10 }} />
          <Skeleton style={{ width: "20%", height: 10 }} />
        </div>
        <Skeleton style={{ width: "80%", height: 26, marginBottom: 20 }} />
        <div className="tour-meta" style={{ borderTop: "none", borderBottom: "none", padding: 0 }}>
          <Skeleton style={{ width: "30%", height: 16 }} />
          <Skeleton style={{ width: "30%", height: 16 }} />
        </div>
      </div>
    </article>
  );
}