import { useState } from "react";
import api from "../../utils/api";

interface ImageUploaderProps {
  onUploadSuccess: (url: string) => void;
  initialImage?: string;
}

export default function ImageUploader({ onUploadSuccess, initialImage }: ImageUploaderProps) {
  const [preview, setPreview] = useState(initialImage || "");
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Basic frontend validation
    if (file.size > 5 * 1024 * 1024) {
      setError("File is too large. Max 5MB.");
      return;
    }

    setUploading(true);
    setError(null);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const token = localStorage.getItem("wwa.token");
      
      // NOTE: Ensure this matches your backend route. 
      // If it's in AdminTourController, it might be /api/v1/admin/tours/upload-image
      const res = await fetch("/api/v1/admin/tours/upload-image", {
        method: "POST",
        headers: { 
          Authorization: `Bearer ${token}` 
        },
        body: formData,
      });

      if (!res.ok) throw new Error("Upload failed");

      const data = await res.json();
      setPreview(data.url);
      onUploadSuccess(data.url);
    } catch (err) {
      setError("Failed to upload image. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="image-uploader">
      <label className={`upload-zone ${uploading ? "uploading" : ""} ${preview ? "has-preview" : ""}`}>
        {preview ? (
          <>
            <img src={preview} alt="Tour cover preview" className="upload-preview" />
            <div className="upload-overlay">
              <span>Click to change image</span>
            </div>
          </>
        ) : (
          <div className="upload-placeholder">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
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
      {error && <p className="form-error" style={{ marginTop: 8 }}>{error}</p>}
    </div>
  );
}