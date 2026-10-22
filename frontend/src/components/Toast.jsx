import { CheckCircle2, XCircle, Info, X } from 'lucide-react';
import useToast from '../hooks/useToast';

const STYLES = {
  success: { ring: 'border-green-200 bg-green-50 text-green-800', Icon: CheckCircle2 },
  error: { ring: 'border-red-200 bg-red-50 text-red-800', Icon: XCircle },
  info: { ring: 'border-blue-200 bg-blue-50 text-blue-800', Icon: Info },
};

/**
 * Renders the active toasts in a fixed stack. Mount once near the app root.
 */
export function ToastContainer() {
  const { toasts, remove } = useToast();

  return (
    <div className="fixed top-4 right-4 z-50 flex w-80 flex-col gap-2">
      {toasts.map((t) => {
        const style = STYLES[t.type] || STYLES.info;
        const { Icon } = style;
        return (
          <div
            key={t.id}
            role="alert"
            className={`flex animate-slide-in-right items-start gap-2 rounded-md border px-3 py-2 shadow-sm ${style.ring}`}
          >
            <Icon size={18} className="mt-0.5 shrink-0" />
            <span className="flex-1 text-sm">{t.message}</span>
            <button
              type="button"
              aria-label="Dismiss notification"
              onClick={() => remove(t.id)}
              className="shrink-0 rounded p-0.5 hover:bg-black/5"
            >
              <X size={16} />
            </button>
          </div>
        );
      })}
    </div>
  );
}

export default ToastContainer;
