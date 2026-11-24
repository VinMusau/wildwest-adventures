import { useState } from "react";
import { uploadTourImage } from "@/lib/uploadImage";

interface ImageUploaderProps {
  onUploadSuccess: (url: string) => void;
  initialImage?: string;
}

export default function ImageUploader({
  onUploadSuccess,
  initialImage,
}: ImageUploaderProps) {
  const [preview, setPreview] = useState(initialImage || "");
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    setError(null);
    try {
      const url = await uploadTourImage(file);
      setPreview(url);
      onUploadSuccess(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="image-uploader">
      <label
        className={`upload-zone ${uploading ? "uploading" : ""} ${preview ? "has-preview" : ""}`}
      >
        {preview ? (
          <>
            <img
              src={preview}
              alt="Tour cover preview"
              className="upload-preview"
            />
            <div className="upload-overlay">
              <span>Click to change image</span>
            </div>
          </>
        ) : (
          <div className="upload-placeholder">
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="17 8 12 3 7 8"></polyline>
              <line x1="12" y1="3" x2="12" y2="15"></line>
            </svg>
            <span>Click to upload cover image</span>
            <small>JPG, PNG or WebP (max 5MB)</small>
          </div>
        )}
        <input
          type="file"
          accept="image/*"
          onChange={handleFileChange}
          disabled={uploading}
          style={{ display: "none" }}
        />
      </label>

      {uploading && <p className="upload-status">Uploading to CDN...</p>}
      {error && (
        <p className="form-error" style={{ marginTop: 8 }}>
          {error}
        </p>
      )}
    </div>
  );
}
