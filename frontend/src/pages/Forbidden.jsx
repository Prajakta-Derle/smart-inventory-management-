import { Link } from 'react-router-dom';
import { ShieldX } from 'lucide-react';

export default function Forbidden() {
  return (
    <div className="flex min-h-full flex-col items-center justify-center gap-3 p-10 text-center">
      <ShieldX size={40} className="text-red-400" />
      <h1 className="text-2xl font-semibold text-slate-800">403 — Access denied</h1>
      <p className="text-sm text-slate-600">
        You don’t have permission to view this page.
      </p>
      <Link to="/dashboard" className="btn-primary">
        Back to dashboard
      </Link>
    </div>
  );
}
