import { createContext, useCallback, useMemo, useState } from 'react';

// eslint-disable-next-line react-refresh/only-export-components
export const ToastContext = createContext(null);

let nextId = 1;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const remove = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback(
    (type, message, timeout = 4000) => {
      const id = nextId++;
      setToasts((prev) => [...prev, { id, type, message }]);
      if (timeout > 0) {
        setTimeout(() => remove(id), timeout);
      }
      return id;
    },
    [remove],
  );

  const value = useMemo(
    () => ({
      toasts,
      remove,
      success: (msg, t) => push('success', msg, t),
      error: (msg, t) => push('error', msg, t),
      info: (msg, t) => push('info', msg, t),
    }),
    [toasts, push, remove],
  );

  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>;
}
