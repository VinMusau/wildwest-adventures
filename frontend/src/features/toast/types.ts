export type ToastType = "success" | "error" | "info";

export interface ToastOptions {
  description?: string;
  /** Milliseconds before auto-dismiss. 0 = sticky. Default 4000. */
  duration?: number;
}

export interface Toast extends ToastOptions {
  id: string;
  type: ToastType;
  title: string;
  leaving?: boolean;
}

export interface ToastContextValue {
  success: (title: string, options?: ToastOptions) => void;
  error: (title: string, options?: ToastOptions) => void;
  info: (title: string, options?: ToastOptions) => void;
  dismiss: (id: string) => void;
}