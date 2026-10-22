import EmptyState from '../../components/EmptyState';
import { formatDateTime } from '../../utils/format';

const DIR_STYLE = {
  ADD: 'bg-green-100 text-green-700',
  REDUCE: 'bg-red-100 text-red-700',
};

/**
 * Compact table of the 10 most recent stock movements.
 */
export default function RecentMovements({ movements = [] }) {
  return (
    <div className="card p-5">
      <h2 className="mb-3 text-sm font-semibold text-slate-700">Recent stock movements</h2>
      {movements.length === 0 ? (
        <EmptyState title="No movements yet" message="Recorded movements will appear here." />
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200">
            <thead>
              <tr className="text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <th className="px-3 py-2">When</th>
                <th className="px-3 py-2">Product</th>
                <th className="px-3 py-2">SKU</th>
                <th className="px-3 py-2">Type</th>
                <th className="px-3 py-2">Dir</th>
                <th className="px-3 py-2 text-right">Qty</th>
                <th className="px-3 py-2">By</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {movements.map((m) => (
                <tr key={m.id}>
                  <td className="px-3 py-2 text-sm text-slate-600">{formatDateTime(m.timestamp)}</td>
                  <td className="px-3 py-2 text-sm text-slate-800">{m.productName}</td>
                  <td className="px-3 py-2 text-sm text-slate-600">{m.sku}</td>
                  <td className="px-3 py-2 text-sm text-slate-600">{m.movementType}</td>
                  <td className="px-3 py-2 text-sm">
                    <span
                      className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${
                        DIR_STYLE[m.direction] || ''
                      }`}
                    >
                      {m.direction}
                    </span>
                  </td>
                  <td className="px-3 py-2 text-right text-sm text-slate-800">{m.quantity}</td>
                  <td className="px-3 py-2 text-sm text-slate-600">{m.actingUsername || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
