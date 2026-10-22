import { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

/**
 * Compact, professional centered modal rendered via a React Portal into
 * document.body — so it escapes the page's layout/overflow constraints and
 * overlays the true viewport.
 *
 *  - size="md" (default): ~750px wide — good for two-column forms.
 *  - size="sm": ~440px — for confirmations.
 */
export function Modal({
  open,
  title,
  subtitle,
  onClose,
  children,
  onConfirm,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  confirmVariant = 'primary',
  busy = false,
  hideFooter = false,
  size = 'md',
}) {
  // Lock background scroll while the modal is open.
  useEffect(() => {
    if (!open) return undefined;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  // Close on Escape.
  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;

  const confirmBtnClass = confirmVariant === 'danger' ? 'btn-danger' : 'btn-primary';
  const widthStyle = size === 'sm' ? 'max-w-[440px]' : 'max-w-[750px]';

  const content = (
    <div
      className="fixed inset-0 z-[1000] flex items-center justify-center overflow-y-auto bg-slate-900/50 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={title}
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        className={`card my-auto flex max-h-[90vh] w-full ${widthStyle} flex-col overflow-hidden rounded-2xl`}
        onMouseDown={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex shrink-0 items-start justify-between gap-4 border-b border-slate-200/70 px-6 py-4">
          <div className="min-w-0">
            <h2 className="text-base font-semibold text-slate-800">{title}</h2>
            {subtitle ? <p className="mt-0.5 text-xs text-slate-500">{subtitle}</p> : null}
          </div>
          <button
            type="button"
            aria-label="Close dialog"
            onClick={onClose}
            className="shrink-0 rounded-lg p-1.5 text-slate-500 shadow-neu-sm transition-colors hover:text-slate-700 active:shadow-neu-inset-sm"
          >
            <X size={18} />
          </button>
        </div>

        {/* Body — scrolls only if content exceeds 90vh */}
        <div className="flex-1 overflow-y-auto px-6 py-5">{children}</div>

        {/* Footer */}
        {!hideFooter ? (
          <div className="flex shrink-0 justify-end gap-3 border-t border-slate-200/70 px-6 py-3.5">
            <button type="button" onClick={onClose} disabled={busy} className="btn-secondary px-5">
              {cancelLabel}
            </button>
            {onConfirm ? (
              <button
                type="button"
                onClick={onConfirm}
                disabled={busy}
                className={`${confirmBtnClass} px-6`}
              >
                {busy ? 'Working…' : confirmLabel}
              </button>
            ) : null}
          </div>
        ) : null}
      </div>
    </div>
  );

  // Portal into document.body so the modal is free of any parent container's
  // width / height / overflow / layout constraints.
  return createPortal(content, document.body);
}

/** Two-column grid for modal form fields; collapses to one column on mobile. */
export function ModalFormGrid({ children }) {
  return <div className="grid grid-cols-1 gap-x-5 gap-y-4 sm:grid-cols-2">{children}</div>;
}

export function ModalFullRow({ children }) {
  return <div className="sm:col-span-2">{children}</div>;
}

export default Modal;
