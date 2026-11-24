import { useState } from "react";
import { uploadTourImage } from "@/lib/uploadImage";
import type { TourImage } from "@/types";

/** Matches the backend's limit (TourWriteRequest.images). */
export const MAX_GALLERY_IMAGES = 30;

interface GalleryUploaderProps {
  images: TourImage[];
  onChange: (images: TourImage[]) => void;
}

/**
 * The tour's gallery photos: upload several at once, add alt text, reorder and
 * remove. The first photos lead the gallery on the tour page.
 */
export default function GalleryUploader({
  images,
  onChange,
}: GalleryUploaderProps) {
  const [progress, setProgress] = useState<{ done: number; total: number }>();
  const [errors, setErrors] = useState<string[]>([]);

  const addFiles = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const picked = Array.from(e.target.files ?? []);
    e.target.value = ""; // allow picking the same files again later
    if (picked.length === 0) return;

    const room = MAX_GALLERY_IMAGES - images.length;
    const files = picked.slice(0, room);
    const problems =
      picked.length > room
        ? [
            `A gallery can have up to ${MAX_GALLERY_IMAGES} photos; ${picked.length - room} not added.`,
          ]
        : [];

    // One at a time; each photo is added as soon as it's uploaded.
    let current = images;
    setProgress({ done: 0, total: files.length });
    for (const [i, file] of files.entries()) {
      try {
        const url = await uploadTourImage(file);
        current = [...current, { imageUrl: url, altText: null }];
        onChange(current);
      } catch (err) {
        problems.push(err instanceof Error ? err.message : "Upload failed.");
      }
      setProgress({ done: i + 1, total: files.length });
    }
    setProgress(undefined);
    setErrors(problems);
  };

  const move = (from: number, to: number) => {
    const next = [...images];
    const [img] = next.splice(from, 1);
    next.splice(to, 0, img);
    onChange(next);
  };

  const setAlt = (index: number, altText: string) =>
    onChange(images.map((img, i) => (i === index ? { ...img, altText } : img)));

  const remove = (index: number) =>
    onChange(images.filter((_, i) => i !== index));

  const uploading = progress !== undefined;

  return (
    <div className="gallery-uploader">
      {images.length > 0 && (
        <ol className="gallery-grid">
          {images.map((img, i) => (
            <li key={`${img.imageUrl}-${i}`} className="gallery-item">
              <div className="gallery-thumb">
                <img src={img.imageUrl} alt={img.altText ?? ""} />
                <span className="gallery-position">{i + 1}</span>
              </div>
              <input
                type="text"
                value={img.altText ?? ""}
                maxLength={200}
                placeholder="Describe the photo (alt text)"
                aria-label={`Alt text for photo ${i + 1}`}
                onChange={(e) => setAlt(i, e.target.value)}
                disabled={uploading}
              />
              <div className="gallery-item-actions">
                <button
                  type="button"
                  onClick={() => move(i, i - 1)}
                  disabled={uploading || i === 0}
                  aria-label={`Move photo ${i + 1} earlier`}
                >
                  ←
                </button>
                <button
                  type="button"
                  onClick={() => move(i, i + 1)}
                  disabled={uploading || i === images.length - 1}
                  aria-label={`Move photo ${i + 1} later`}
                >
                  →
                </button>
                <button
                  type="button"
                  className="gallery-remove"
                  onClick={() => remove(i)}
                  disabled={uploading}
                >
                  Remove
                </button>
              </div>
            </li>
          ))}
        </ol>
      )}

      <label
        className={`button ghost gallery-add${uploading || images.length >= MAX_GALLERY_IMAGES ? " disabled" : ""}`}
      >
        {uploading
          ? `Uploading ${Math.min(progress.done + 1, progress.total)} of ${progress.total}…`
          : images.length >= MAX_GALLERY_IMAGES
            ? `Gallery full (${MAX_GALLERY_IMAGES} photos)`
            : "Add photos"}
        <input
          type="file"
          accept="image/*"
          multiple
          onChange={addFiles}
          disabled={uploading || images.length >= MAX_GALLERY_IMAGES}
          style={{ display: "none" }}
        />
      </label>
      <small className="text-muted">
        JPG, PNG or WebP, max 5 MB each. The first three appear at the top of
        the tour page.
      </small>

      {errors.length > 0 && (
        <ul className="form-error gallery-errors">
          {errors.map((e) => (
            <li key={e}>{e}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
