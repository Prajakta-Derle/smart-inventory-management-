import { Outlet } from 'react-router-dom';
import { Boxes } from 'lucide-react';

/**
 * Centered card layout for the login/register pages.
 */
export function AuthLayout() {
  return (
    <div className="flex min-h-full items-center justify-center bg-neu-base px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 flex flex-col items-center gap-3">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-neu-base text-brand-600 shadow-neu">
            <Boxes size={28} />
          </div>
          <h1 className="text-lg font-semibold text-slate-700">Smart Inventory Management</h1>
          <p className="text-sm text-slate-500">Sign in to your workspace</p>
        </div>
        <div className="card animate-scale-in p-7">
          <Outlet />
        </div>
      </div>
    </div>
  );
}

export default AuthLayout;
