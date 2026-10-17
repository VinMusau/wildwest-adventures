export interface ConfirmInput {
  placeholder?: string;
  defaultValue?: string;
  required?: boolean;
}

export interface ConfirmOptions {
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: "primary" | "danger";
  input?: ConfirmInput;
}

export interface ConfirmContextValue {
  /**
   * Opens a confirmation modal.
   * - If `options.input` is provided, resolves to the string value (or null if cancelled).
   * - Otherwise, resolves to `true` (confirmed) or `false` (cancelled).
   */
  confirm: {
    (options: ConfirmOptions & { input: ConfirmInput }): Promise<string | null>;
    (options: ConfirmOptions & { input?: undefined }): Promise<boolean>;
  };
}