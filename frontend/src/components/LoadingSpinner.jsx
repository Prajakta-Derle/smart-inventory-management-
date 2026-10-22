import { Loader2 } from 'lucide-react';

/**
 * Simple spinner shown during in-flight requests.
 */
export function LoadingSpinner({ label = 'Loading…', className = '' }) {
  return (
    <div
      className={`flex items-center justify-center gap-2 py-8 text-slate-500 ${className}`}
      role="status"
      aria-live="polite"
    >
      <Loader2 className="animate-spin" size={20} />
      <span className="text-sm">{label}</span>
    </div>
  );
}

export default LoadingSpinner;
