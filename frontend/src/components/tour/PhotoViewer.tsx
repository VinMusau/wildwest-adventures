import { useEffect } from "react";
import type { TourImage } from "@/types";

interface PhotoViewerProps {
  title: string;
  images: TourImage[];
  onClose: () => void;
}

/** Every photo of a tour, full width, over the page. Escape or the button closes it. */
export default function PhotoViewer({
  title,
  images,
  onClose,
}: PhotoViewerProps) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    // Stop the page behind from scrolling while the viewer is open.
    const overflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", onKey);
      document.body.style.overflow = overflow;
    };
  }, [onClose]);

  return (
    <div
      className="photo-viewer"
      role="dialog"
      aria-modal="true"
      aria-label={`${title}: all photos`}
    >
      <button className="photo-viewer-close" onClick={onClose} autoFocus>
        Close
      </button>
      <ol className="photo-viewer-list">
        {images.map((img, i) => (
          <li key={`${img.imageUrl}-${i}`}>
            <figure style={{ margin: 0 }}>
              <img
                src={img.imageUrl}
                alt={img.altText ?? title}
                loading="lazy"
              />
              {img.altText && <figcaption>{img.altText}</figcaption>}
            </figure>
          </li>
        ))}
      </ol>
    </div>
  );
}
