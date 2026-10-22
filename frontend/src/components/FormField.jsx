/**
 * Wraps a labelled input and shows a field-level validation error.
 * Pass the input (or any control) as children.
 */
export function FormField({ label, htmlFor, error, required = false, children, hint }) {
  return (
    <div className="flex flex-col gap-1">
      {label ? (
        <label htmlFor={htmlFor} className="text-sm font-medium text-slate-700">
          {label}
          {required ? <span className="ml-0.5 text-red-500">*</span> : null}
        </label>
      ) : null}
      {children}
      {hint && !error ? <p className="text-xs text-slate-500">{hint}</p> : null}
      {error ? (
        <p className="text-xs text-red-600" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
}

export default FormField;
