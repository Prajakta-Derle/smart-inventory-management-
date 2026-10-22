import { AlertTriangle } from 'lucide-react';

/**
 * Inline API error display. Accepts a string or a normalised error object.
 */
export function ErrorMessage({ error, className = '' }) {
  if (!error) return null;
  const message = typeof error === 'string' ? error : error.message || 'Something went wrong';

  return (
    <div
      role="alert"
      className={`flex items-start gap-2 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800 ${className}`}
    >
      <AlertTriangle size={16} className="mt-0.5 shrink-0" />
      <span>{message}</span>
    </div>
  );
}

export default ErrorMessage;
