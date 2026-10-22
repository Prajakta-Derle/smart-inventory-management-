import { Inbox } from 'lucide-react';

/**
 * Shown when a list returns zero results.
 */
export function EmptyState({ title = 'Nothing here yet', message = '', icon: Icon = Inbox }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 py-12 text-center text-slate-500">
      <Icon size={32} className="text-slate-400" />
      <p className="text-sm font-medium text-slate-700">{title}</p>
      {message ? <p className="text-sm">{message}</p> : null}
    </div>
  );
}

export default EmptyState;
