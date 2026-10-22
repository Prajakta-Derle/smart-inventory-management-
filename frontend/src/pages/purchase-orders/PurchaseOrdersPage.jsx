import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Eye } from 'lucide-react';
import purchaseOrdersApi from '../../api/purchaseOrdersApi';
import suppliersApi from '../../api/suppliersApi';
import DataTable from '../../components/DataTable';
import { formatCurrency, formatDate } from '../../utils/format';

const PAGE_SIZE = 10;
const STATUSES = ['DRAFT', 'ORDERED', 'RECEIVED', 'CANCELLED'];

const STATUS_STYLE = {
  DRAFT: 'bg-slate-100 text-slate-600',
  ORDERED: 'bg-blue-100 text-blue-700',
  RECEIVED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export function StatusBadge({ status }) {
  return (
    <span
      className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${
        STATUS_STYLE[status] || 'bg-slate-100 text-slate-600'
      }`}
    >
      {status}
    </span>
  );
}

export default function PurchaseOrdersPage() {
  const navigate = useNavigate();

  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0);
  const [meta, setMeta] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [suppliers, setSuppliers] = useState([]);

  // filters + sort
  const [supplierId, setSupplierId] = useState('');
  const [status, setStatus] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [orderNumber, setOrderNumber] = useState('');
  const [sortBy, setSortBy] = useState('orderDate');
  const [sortDir, setSortDir] = useState('desc');

  useEffect(() => {
    // Load active suppliers for the filter dropdown.
    suppliersApi
      .list({ page: 0, size: 200, active: 'true' })
      .then((data) => setSuppliers(data.content || []))
      .catch(() => setSuppliers([]));
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE, sortBy, sortDir };
      if (supplierId) params.supplierId = supplierId;
      if (status) params.status = status;
      if (startDate) params.startDate = startDate;
      if (endDate) params.endDate = endDate;
      if (orderNumber.trim()) params.orderNumber = orderNumber.trim();
      const data = await purchaseOrdersApi.list(params);
      setRows(data.content || []);
      setMeta({ totalPages: data.totalPages, totalElements: data.totalElements });
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [page, sortBy, sortDir, supplierId, status, startDate, endDate, orderNumber]);

  useEffect(() => {
    load();
  }, [load]);

  const onSortChange = (key) => {
    if (sortBy === key) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    else {
      setSortBy(key);
      setSortDir('asc');
    }
    setPage(0);
  };

  const onSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    load();
  };

  const inputClass = 'input';

  const columns = [
    { key: 'orderNumber', header: 'Order #' },
    { key: 'supplierName', header: 'Supplier', render: (r) => r.supplierName || '—' },
    { key: 'orderDate', header: 'Order Date', sortable: true, render: (r) => formatDate(r.orderDate) },
    { key: 'expectedDeliveryDate', header: 'Expected', render: (r) => formatDate(r.expectedDeliveryDate) },
    { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.status} /> },
    { key: 'total', header: 'Total', align: 'right', sortable: true, render: (r) => formatCurrency(r.total) },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (r) => (
        <Link
          to={`/purchase-orders/${r.id}`}
          className="inline-flex items-center gap-1 rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
        >
          <Eye size={14} /> View
        </Link>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Purchase Orders</h1>
        <button
          type="button"
          onClick={() => navigate('/purchase-orders/new')}
          className="btn-primary"
        >
          <Plus size={16} /> Create Order
        </button>
      </div>

      <form onSubmit={onSearchSubmit} className="flex flex-wrap items-end gap-3">
        <div className="flex min-w-[180px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Supplier</label>
          <select className={inputClass} value={supplierId}
            onChange={(e) => { setSupplierId(e.target.value); setPage(0); }}>
            <option value="">All</option>
            {suppliers.map((s) => (
              <option key={s.id} value={s.id}>{s.supplierName}</option>
            ))}
          </select>
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Status</label>
          <select className={inputClass} value={status}
            onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
            <option value="">All</option>
            {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">From</label>
          <input type="date" className={inputClass} value={startDate}
            onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">To</label>
          <input type="date" className={inputClass} value={endDate}
            onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Order #</label>
          <input className={inputClass} value={orderNumber}
            onChange={(e) => setOrderNumber(e.target.value)} />
        </div>
        <button type="submit"
          className="rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
          Search
        </button>
      </form>

      <DataTable
        columns={columns}
        rows={rows}
        loading={loading}
        error={error}
        page={page}
        totalPages={meta.totalPages}
        totalElements={meta.totalElements}
        onPageChange={setPage}
        sortBy={sortBy}
        sortDir={sortDir}
        onSortChange={onSortChange}
        emptyTitle="No purchase orders"
        emptyMessage="Create an order or adjust your filters."
      />
    </div>
  );
}
