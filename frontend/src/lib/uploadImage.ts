import { apiUpload } from "@/lib/api";

export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

/**
 * Uploads one image to the CDN (AdminTourController: POST
 * /api/v1/admin/tours/upload-image) and returns its URL. Throws an Error with
 * a message to show the admin if the file is unsuitable or the upload fails.
 */
export async function uploadTourImage(file: File): Promise<string> {
  if (!file.type.startsWith("image/")) {
    throw new Error(`${file.name} isn't an image.`);
  }
  if (file.size > MAX_IMAGE_BYTES) {
    throw new Error(`${file.name} is too large (max 5 MB).`);
  }
  const formData = new FormData();
  formData.append("file", file);
  try {
    const { url } = await apiUpload<{ url: string }>(
      "/v1/admin/tours/upload-image",
      formData,
    );
    return url;
  } catch {
    // A 401 is already handled by the shared client (AUTH_EXPIRED_EVENT).
    throw new Error(`Couldn't upload ${file.name}. Please try again.`);
  }
}
