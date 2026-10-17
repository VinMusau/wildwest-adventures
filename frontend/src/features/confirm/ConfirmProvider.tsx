import { useCallback, useMemo, useState } from "react";
import { createPortal } from "react-dom";
import ConfirmModal from "./ConfirmModal";
import { ConfirmContext } from "./ConfirmContext";
import type { ConfirmOptions } from "./types";

export default function ConfirmProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<{
    options: ConfirmOptions;
    resolve: (val: any) => void;
  } | null>(null);

  const confirm = useCallback((options: ConfirmOptions) => {
    return new Promise<any>((resolve) => {
      setState({ options, resolve });
    });
  }, []);

  const handleResolve = useCallback(
    (value: string | boolean | null) => {
      state?.resolve(value);
      setState(null);
    },
    [state]
  );

  const contextValue = useMemo(() => ({ confirm }), [confirm]);

  return (
    <ConfirmContext.Provider value={contextValue}>
      {children}
      {state &&
        createPortal(
          <ConfirmModal
            options={state.options}
            onResolve={handleResolve}
          />,
          document.body
        )}
    </ConfirmContext.Provider>
  );
}