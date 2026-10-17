import { useEffect, useRef, useState } from "react";
import Button from "@/components/ui/Button";
import type { ConfirmOptions } from "./types";

interface ConfirmModalProps {
  options: ConfirmOptions;
  onResolve: (value: string | boolean | null) => void;
}

export default function ConfirmModal({ options, onResolve }: ConfirmModalProps) {
  const [inputValue, setInputValue] = useState(options.input?.defaultValue ?? "");
  const inputRef = useRef<HTMLInputElement>(null);
  const confirmBtnRef = useRef<HTMLButtonElement>(null);

  const hasInput = Boolean(options.input);
  const isDanger = options.variant === "danger";

  // Focus management on open
  useEffect(() => {
    if (hasInput) inputRef.current?.focus();
    else confirmBtnRef.current?.focus();
  }, [hasInput]);

  // Handle ESC
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        e.preventDefault();
        onResolve(hasInput ? null : false);
      }
    };
    document.addEventListener("keydown", handler);
    return () => document.removeEventListener("keydown", handler);
  }, [hasInput, onResolve]);

  const handleConfirm = (e: React.FormEvent) => {
    e.preventDefault();
    if (hasInput) {
      if (options.input?.required && !inputValue.trim()) return;
      onResolve(inputValue.trim());
    } else {
      onResolve(true);
    }
  };

  const handleCancel = () => {
    onResolve(hasInput ? null : false);
  };

  return (
    <div className="confirm-backdrop" onClick={handleCancel}>
      <form
        className="confirm-modal"
        onClick={(e) => e.stopPropagation()}
        onSubmit={handleConfirm}
      >
        <div className="confirm-header">
          <h2>{options.title}</h2>
          {options.description && <p>{options.description}</p>}
        </div>

        {hasInput && (
          <div className="confirm-input-wrap">
            <input
              ref={inputRef}
              type="text"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              placeholder={options.input?.placeholder}
              className="confirm-input"
            />
          </div>
        )}

        <div className="confirm-actions">
          <Button type="button" variant="ghost" onClick={handleCancel}>
            {options.cancelLabel ?? "Cancel"}
          </Button>
          <button
            ref={confirmBtnRef}
            type="submit"
            className={`button ${isDanger ? "danger-btn" : "primary"}`}
            disabled={hasInput && options.input?.required && !inputValue.trim()}
          >
            {options.confirmLabel ?? "Confirm"}
          </button>
        </div>
      </form>
    </div>
  );
}