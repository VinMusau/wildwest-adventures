import { useCallback, useMemo, useRef, useState } from "react";
import { createPortal } from "react-dom";
import ToastItem from "./ToastItem";
import { ToastContext } from "./ToastContext";
import type { Toast, ToastOptions, ToastType } from "./types";

const DEFAULT_DURATION = 4000;
const LEAVE_ANIMATION_MS = 220;

export default function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const timers = useRef<Record<string, number>>({});

  const dismiss = useCallback((id: string) => {
    const timer = timers.current[id];
    if (timer) {
      window.clearTimeout(timer);
      delete timers.current[id];
    }

    setToasts((prev) =>
      prev.map((t) => (t.id === id ? { ...t, leaving: true } : t))
    );

    // Remove after exit animation completes
    window.setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, LEAVE_ANIMATION_MS);
  }, []);

  const push = useCallback(
    (type: ToastType, title: string, options?: ToastOptions) => {
      const id = crypto.randomUUID();
      const toast: Toast = {
        id,
        type,
        title,
        description: options?.description,
        duration: options?.duration ?? DEFAULT_DURATION,
      };
      setToasts((prev) => [...prev, toast]);

      if ((toast.duration ?? 0) > 0) {
        timers.current[id] = window.setTimeout(
          () => dismiss(id),
          toast.duration
        );
      }
    },
    [dismiss]
  );

  const value = useMemo(
    () => ({
      success: (title: string, options?: ToastOptions) =>
        push("success", title, options),
      error: (title: string, options?: ToastOptions) =>
        push("error", title, options),
      info: (title: string, options?: ToastOptions) =>
        push("info", title, options),
      dismiss,
    }),
    [push, dismiss]
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      {createPortal(
        <div className="toast-viewport" aria-live="polite" aria-atomic="false">
          {toasts.map((t) => (
            <ToastItem key={t.id} toast={t} onDismiss={() => dismiss(t.id)} />
          ))}
        </div>,
        document.body
      )}
    </ToastContext.Provider>
  );
}