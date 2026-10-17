import { Link } from "react-router";
import Button from "@/components/ui/Button";
import Icon from "@/components/ui/Icon";
import { useAdminTours, useDeleteTour } from "@/features/admin/useAdminTours";
import { formatPrice } from "@/lib/format";
import { useToast } from "@/features/toast/useToast";

export default function AdminToursPage() {
  const { data: tours = [], isLoading } = useAdminTours();
  const deleteTour = useDeleteTour();
  const toast = useToast();

  const handleDelete = (slug: string, title: string) => {
    if (!window.confirm(`Delete "${title}"? This cannot be undone.`)) return;
    deleteTour.mutate(slug, {
      onSuccess: () => toast.success("Tour deleted", { description: title }),
      onError: () => toast.error("Could not delete tour"),
    });
  };

  return (
    <div className="admin-page">
      <div className="admin-page-head">
        <div>
          <span className="eyebrow">Content</span>
          <h1>Tours</h1>
        </div>
        <Link to="/admin/tours/new" className="button primary">
          New tour <Icon name="arrow" />
        </Link>
      </div>

      {isLoading ? (
        <p className="text-muted">Loading tours…</p>
      ) : tours.length === 0 ? (
        <p className="text-muted">No tours yet. Create your first one.</p>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Order</th>
                <th>Title</th>
                <th>Destination</th>
                <th>Duration</th>
                <th>Price</th>
                <th>Featured</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {tours.map((tour) => (
                <tr key={tour.slug}>
                  <td className="admin-cell-num">{tour.displayOrder}</td>
                  <td>
                    <Link to={`/admin/tours/${tour.slug}/edit`} className="admin-link">
                      {tour.title}
                    </Link>
                    <small className="admin-slug">{tour.slug}</small>
                  </td>
                  <td>{tour.destinationName}</td>
                  <td>{tour.durationDays} days</td>
                  <td>{formatPrice(tour.priceCents, tour.currency)}</td>
                  <td>
                    {tour.featured ? (
                      <span className="badge-yes">Yes</span>
                    ) : (
                      <span className="badge-no">—</span>
                    )}
                  </td>
                  <td className="admin-cell-actions">
                    <Link to={`/admin/tours/${tour.slug}/edit`}>Edit</Link>
                    <button
                      className="danger"
                      onClick={() => handleDelete(tour.slug, tour.title)}
                      disabled={deleteTour.isPending}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}