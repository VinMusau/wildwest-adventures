export default function BookingRowSkeleton() {
  return (
    <article className="booking-row">
      {/* Image placeholder */}
      <div 
        className="skeleton" 
        style={{ width: 140, height: 100, borderRadius: 8 }} 
      />
      
      {/* Body text placeholders */}
      <div className="booking-row-body">
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 12 }}>
          <div className="skeleton" style={{ width: 80, height: 12 }} />
          <div className="skeleton" style={{ width: 60, height: 20, borderRadius: 999 }} />
        </div>
        <div className="skeleton" style={{ width: "60%", height: 24, marginBottom: 12 }} />
        <div className="skeleton" style={{ width: "40%", height: 14 }} />
      </div>

      {/* Price & Actions placeholders */}
      <div className="booking-row-price">
        <div 
          className="skeleton" 
          style={{ width: 80, height: 18, marginBottom: 12, marginLeft: "auto" }} 
        />
        <div 
          className="skeleton" 
          style={{ width: 60, height: 14, marginLeft: "auto" }} 
        />
      </div>
    </article>
  );
}