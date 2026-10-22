import { Link } from 'react-router-dom';
import { Compass } from 'lucide-react';

export default function NotFound() {
  return (
    <div className="flex min-h-full flex-col items-center justify-center gap-3 p-10 text-center">
      <Compass size={40} className="text-slate-400" />
      <h1 className="text-2xl font-semibold text-slate-800">404 — Page not found</h1>
      <p className="text-sm text-slate-600">
        The page you are looking for doesn’t exist or has moved.
      </p>
      <Link to="/dashboard" className="btn-primary">
        Back to dashboard
      </Link>
    </div>
  );
}
