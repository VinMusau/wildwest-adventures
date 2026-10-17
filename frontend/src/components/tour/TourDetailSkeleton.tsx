import Skeleton from "@/components/ui/Skeleton";

export default function TourDetailSkeleton() {
  return (
    <div className="detail-page-skeleton">
      {/* Breadcrumb / Title Area */}
      <div className="detail-title">
        <div>
          <Skeleton style={{ width: 200, height: 12, marginBottom: 12 }} />
          <Skeleton style={{ width: 400, height: 48 }} />
        </div>
      </div>

      {/* Gallery */}
      <div className="gallery" style={{ marginTop: 24 }}>
        <Skeleton style={{ width: "100%", height: "100%", borderRadius: "12px 0 0 12px" }} />
        <Skeleton style={{ width: "100%", height: "100%", borderRadius: 0 }} />
        <Skeleton style={{ width: "100%", height: "100%", borderRadius: "0 12px 12px 0" }} />
      </div>

      {/* Main Content + Sidebar */}
      <div className="detail-layout">
        <div className="detail-main">
          <Skeleton style={{ width: "80%", height: 28, marginBottom: 40 }} />
          <Skeleton style={{ width: "100%", height: 16, marginBottom: 12 }} />
          <Skeleton style={{ width: "100%", height: 16, marginBottom: 12 }} />
          <Skeleton style={{ width: "60%", height: 16, marginBottom: 60 }} />

          <Skeleton style={{ width: 200, height: 32, marginBottom: 24 }} />
          <Skeleton style={{ width: "100%", height: 100, marginBottom: 16, borderRadius: 8 }} />
          <Skeleton style={{ width: "100%", height: 100, marginBottom: 16, borderRadius: 8 }} />
          <Skeleton style={{ width: "100%", height: 100, marginBottom: 16, borderRadius: 8 }} />
        </div>

        {/* Sticky Booking Card Skeleton */}
        <aside className="booking-card">
          <Skeleton style={{ width: "30%", height: 12, marginBottom: 8 }} />
          <Skeleton style={{ width: "60%", height: 32, marginBottom: 24 }} />
          <Skeleton style={{ width: "100%", height: 48, marginBottom: 16 }} />
          <Skeleton style={{ width: "100%", height: 48, marginBottom: 24 }} />
          <Skeleton style={{ width: "100%", height: 48, borderRadius: 8 }} />
        </aside>
      </div>
    </div>
  );
}