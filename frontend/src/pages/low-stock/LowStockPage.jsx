import { useCallback, useEffect, useState } from 'react';
import { AlertTriangle, PackageX } from 'lucide-react';
import lowStockApi from '../../api/lowStockApi';
import DataTable from '../../components/DataTable';

const PAGE_SIZE = 10;
const FILTERS = [
  { value: '', label: 'All' },
  { value: 'LOW_STOCK', label: 'Low stock' },
  { value: 'OUT_OF_STOCK', label: 'Out of stock' },
];

function StatusBadge({ status }) {
  const style =
    status === 'OUT_OF_STOCK' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700';
  return (
    <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${style}`}>
      {status === 'OUT_OF_STOCK' ? 'Out of stock' : 'Low stock'}
    </span>
  );
}

function SummaryCard({ icon: Icon, label, value, tone }) {
  return (
    <div className="flex items-center gap-3 card px-5 py-4">
      <div className={`flex h-10 w-10 items-center justify-center rounded-md ${tone}`}>
        <Icon size={20} />
      </div>
      <div>
        <div className="text-2xl font-semibold text-slate-800">{value}</div>
        <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</div>
      </div>
    </div>
  );
}

export default function LowStockPage() {
  const [summary, setSummary] = useState({ lowStockCount: 0, outOfStockCount: 0 });

  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0);
  const [meta, setMeta] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    lowStockApi
      .summary()
      .then((data) =>
        setSummary({
          lowStockCount: data.lowStockCount ?? 0,
          outOfStockCount: data.outOfStockCount ?? 0,
        }),
      )
      .catch(() => {});
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE };
      if (filter) params['stock-status'] = filter;
      const data = await lowStockApi.list(params);
      setRows(data.content || []);
      setMeta({ totalPages: data.totalPages, totalElements: data.totalElements });
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [page, filter]);

  useEffect(() => {
    load();
  }, [load]);

  const columns = [
    { key: 'productName', header: 'Product' },
    { key: 'sku', header: 'SKU' },
    { key: 'categoryName', header: 'Category', render: (r) => r.categoryName || '—' },
    { key: 'currentStock', header: 'Current', align: 'right' },
    { key: 'minStockLevel', header: 'Min level', align: 'right' },
    { key: 'stockStatus', header: 'Status', render: (r) => <StatusBadge status={r.stockStatus} /> },
  ];

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-semibold text-slate-800">Low Stock</h1>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <SummaryCard
          icon={AlertTriangle}
          label="Low stock"
          value={summary.lowStockCount}
          tone="bg-amber-100 text-amber-700"
        />
        <SummaryCard
          icon={PackageX}
          label="Out of stock"
          value={summary.outOfStockCount}
          tone="bg-red-100 text-red-700"
        />
      </div>

      <div className="flex gap-1">
        {FILTERS.map((f) => (
          <button
            key={f.value || 'all'}
            type="button"
            onClick={() => {
              setFilter(f.value);
              setPage(0);
            }}
            className={`rounded-lg px-3 py-1.5 text-sm font-medium ${
              filter === f.value
                ? 'bg-brand-600 text-white'
                : 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        loading={loading}
        error={error}
        page={page}
        totalPages={meta.totalPages}
        totalElements={meta.totalElements}
        onPageChange={setPage}
        rowKey={(r) => r.productId}
        emptyTitle="Nothing low on stock"
        emptyMessage="All tracked products are above their minimum levels."
      />
    </div>
  );
}
