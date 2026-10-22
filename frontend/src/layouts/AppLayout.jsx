import { useState } from 'react';
import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Package,
  Tags,
  Truck,
  ClipboardList,
  ArrowLeftRight,
  AlertTriangle,
  FileBarChart,
  Bot,
  Users,
  LogOut,
  Menu,
  Boxes,
} from 'lucide-react';
import useAuth from '../hooks/useAuth';

// Each item lists the roles allowed to see it. Items are filtered by role.
const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'STAFF'] },
  { to: '/products', label: 'Products', icon: Package, roles: ['ADMIN', 'STAFF'] },
  { to: '/categories', label: 'Categories', icon: Tags, roles: ['ADMIN'] },
  { to: '/suppliers', label: 'Suppliers', icon: Truck, roles: ['ADMIN'] },
  { to: '/purchase-orders', label: 'Purchase Orders', icon: ClipboardList, roles: ['ADMIN', 'STAFF'] },
  { to: '/stock-movements', label: 'Stock Movements', icon: ArrowLeftRight, roles: ['ADMIN', 'STAFF'] },
  { to: '/low-stock', label: 'Low Stock', icon: AlertTriangle, roles: ['ADMIN', 'STAFF'] },
  { to: '/reports', label: 'Reports', icon: FileBarChart, roles: ['ADMIN', 'STAFF'] },
  { to: '/ai-assistant', label: 'AI Assistant', icon: Bot, roles: ['ADMIN', 'STAFF'] },
  { to: '/users', label: 'Users', icon: Users, roles: ['ADMIN'] },
];

/**
 * Persistent sidebar + content outlet. Sidebar items are filtered by the
 * authenticated user's role; collapses to a toggle on narrow viewports.
 */
export function AppLayout() {
  const { role, username, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);

  const items = NAV_ITEMS.filter((item) => item.roles.includes(role));

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const linkClass = ({ isActive }) =>
    `flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-medium transition-all ${
      isActive
        ? 'text-brand-700 shadow-neu-inset-sm'
        : 'text-slate-500 hover:text-brand-600'
    }`;

  return (
    <div className="flex min-h-full">
      {/* Mobile backdrop — tap outside the open sidebar to close it */}
      {open ? (
        <div
          className="fixed inset-0 z-20 bg-black/40 md:hidden"
          aria-hidden="true"
          onClick={() => setOpen(false)}
        />
      ) : null}

      {/* Sidebar */}
      <aside
        className={`${
          open ? 'block' : 'hidden'
        } fixed inset-y-0 left-0 z-30 w-64 bg-neu-base md:static md:block`}
      >
        <div className="flex h-16 items-center gap-2.5 px-5">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-neu-base text-brand-600 shadow-neu-sm">
            <Boxes size={20} />
          </div>
          <span className="text-base font-semibold text-slate-700">Inventory</span>
        </div>
        <nav className="flex flex-col gap-1 p-3">
          {items.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={linkClass}
                onClick={() => setOpen(false)}
              >
                <Icon size={18} />
                {item.label}
              </NavLink>
            );
          })}
        </nav>
      </aside>

      {/* Main column */}
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 items-center justify-between bg-neu-base px-4 md:px-6">
          <button
            type="button"
            aria-label="Toggle navigation"
            className="rounded-xl p-2 text-slate-600 shadow-neu-sm active:shadow-neu-inset-sm md:hidden"
            onClick={() => setOpen((v) => !v)}
          >
            <Menu size={20} />
          </button>
          <div className="ml-auto flex items-center gap-4">
            <span className="flex items-center gap-2 text-sm text-slate-600">
              <span className="hidden sm:inline">{username}</span>
              <span className="badge-brand">{role}</span>
            </span>
            <button type="button" onClick={handleLogout} className="btn-secondary">
              <LogOut size={16} /> Logout
            </button>
          </div>
        </header>

        <main className="flex-1 overflow-x-hidden p-4 md:p-6">
          <div key={location.pathname} className="animate-fade-up">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
