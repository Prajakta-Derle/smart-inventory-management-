import { Link } from 'react-router-dom';
import { AlertTriangle } from 'lucide-react';
import EmptyState from '../../components/EmptyState';

function StatusBadge({ status }) {
  const style =
    status === 'OUT_OF_STOCK' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700';
  return (
    <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${style}`}>
      {status === 'OUT_OF_STOCK' ? 'Out' : 'Low'}
    </span>
  );
}

/**
 * Lists the most critical low/out-of-stock products (summary top-10).
 */
export default function LowStockPanel({ items = [] }) {
  return (
    <div className="card p-5">
      <div className="mb-3 flex items-center justify-between">
        <h2 className="flex items-center gap-2 text-sm font-semibold text-slate-700">
          <AlertTriangle size={16} className="text-amber-500" /> Low-stock alerts
        </h2>
        <Link to="/low-stock" className="text-xs font-medium text-brand-600 hover:underline">
          View all
        </Link>
      </div>

      {items.length === 0 ? (
        <EmptyState title="All good" message="No products are low on stock." />
      ) : (
        <ul className="divide-y divide-slate-100">
          {items.map((it) => (
            <li key={it.productId} className="flex items-center justify-between py-2">
              <div className="min-w-0">
                <div className="truncate text-sm font-medium text-slate-800">{it.productName}</div>
                <div className="text-xs text-slate-500">
                  {it.sku} · {it.currentStock}/{it.minStockLevel}
                </div>
              </div>
              <StatusBadge status={it.stockStatus} />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
