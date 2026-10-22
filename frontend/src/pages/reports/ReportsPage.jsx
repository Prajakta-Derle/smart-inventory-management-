import { useState } from 'react';
import reportsApi from '../../api/reportsApi';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import EmptyState from '../../components/EmptyState';
import { formatCurrency, formatDate, formatDateTime } from '../../utils/format';

const isoDaysAgo = (days) => {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d.toISOString().slice(0, 10);
};
const todayISO = () => new Date().toISOString().slice(0, 10);

const TABS = [
  { key: 'purchase', label: 'Purchase', dated: true },
  { key: 'inventory', label: 'Inventory', dated: true },
  { key: 'supplier', label: 'Supplier Purchase', dated: true },
  { key: 'movements', label: 'Stock Movements', dated: true },
  { key: 'lowstock', label: 'Low Stock', dated: false },
];

const MOVEMENT_TYPES = ['', 'PURCHASE', 'SALE', 'DAMAGED', 'ADJUSTMENT'];

function Table({ columns, rows, rowKey }) {
  if (!rows || rows.length === 0) {
    return <EmptyState title="No data" message="No records for this report." />;
  }
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            {columns.map((c) => (
              <th key={c.key}
                className={`px-4 py-2.5 text-xs font-semibold uppercase tracking-wide text-slate-600 ${
                  c.align === 'right' ? 'text-right' : 'text-left'
                }`}>
                {c.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {rows.map((r, i) => (
            <tr key={rowKey ? rowKey(r, i) : i}>
              {columns.map((c) => (
                <td key={c.key}
                  className={`px-4 py-2.5 text-sm text-slate-700 ${c.align === 'right' ? 'text-right' : 'text-left'}`}>
                  {c.render ? c.render(r) : r[c.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default function ReportsPage() {
  const [tab, setTab] = useState('purchase');
  const [startDate, setStartDate] = useState(isoDaysAgo(30));
  const [endDate, setEndDate] = useState(todayISO());
  const [movementType, setMovementType] = useState('');

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [dateError, setDateError] = useState(null);

  const activeTab = TABS.find((t) => t.key === tab);

  const switchTab = (key) => {
    setTab(key);
    setResult(null);
    setError(null);
    setDateError(null);
  };

  const generate = async () => {
    setError(null);
    setDateError(null);
    if (activeTab.dated && startDate && endDate && startDate > endDate) {
      setDateError('Start date must be on or before end date');
      return;
    }
    setLoading(true);
    try {
      let data;
      if (tab === 'purchase') data = await reportsApi.purchase(startDate, endDate);
      else if (tab === 'inventory') data = await reportsApi.inventory(startDate, endDate);
      else if (tab === 'supplier') data = await reportsApi.supplierPurchase(startDate, endDate);
      else if (tab === 'movements') data = await reportsApi.stockMovements(startDate, endDate, movementType || undefined);
      else if (tab === 'lowstock') data = await reportsApi.lowStock();
      setResult(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  const inputClass = 'input py-1.5';

  const renderResult = () => {
    if (loading) return <LoadingSpinner />;
    if (error) return <ErrorMessage error={error} />;
    if (result == null) return <p className="text-sm text-slate-500">Choose options and generate the report.</p>;

    if (tab === 'purchase') {
      return (
        <div className="flex flex-col gap-4">
          <div className="flex flex-wrap gap-6 card p-4 text-sm">
            <div><span className="text-slate-500">Total orders:</span> <span className="font-semibold">{result.totalOrderCount}</span></div>
            <div><span className="text-slate-500">Total amount:</span> <span className="font-semibold">{formatCurrency(result.totalAmount)}</span></div>
          </div>
          <Table
            columns={[
              { key: 'orderNumber', header: 'Order #' },
              { key: 'supplierName', header: 'Supplier' },
              { key: 'orderDate', header: 'Date', render: (r) => formatDate(r.orderDate) },
              { key: 'status', header: 'Status' },
              { key: 'total', header: 'Total', align: 'right', render: (r) => formatCurrency(r.total) },
            ]}
            rows={result.orders}
            rowKey={(r) => r.orderNumber}
          />
        </div>
      );
    }
    if (tab === 'inventory') {
      return (
        <Table
          columns={[
            { key: 'productName', header: 'Product' },
            { key: 'sku', header: 'SKU' },
            { key: 'currentStock', header: 'Stock', align: 'right' },
            { key: 'minStockLevel', header: 'Min', align: 'right' },
            { key: 'maxStockLevel', header: 'Max', align: 'right' },
            { key: 'totalPurchaseQuantity', header: 'Purchased', align: 'right' },
            { key: 'totalSaleQuantity', header: 'Sold', align: 'right' },
            { key: 'totalDamagedQuantity', header: 'Damaged', align: 'right' },
            { key: 'totalAdjustmentQuantity', header: 'Adjusted', align: 'right' },
          ]}
          rows={result}
          rowKey={(r) => r.productId}
        />
      );
    }
    if (tab === 'supplier') {
      return (
        <Table
          columns={[
            { key: 'supplierName', header: 'Supplier' },
            { key: 'companyName', header: 'Company' },
            { key: 'purchaseOrderCount', header: 'Orders', align: 'right' },
            { key: 'totalValue', header: 'Total value', align: 'right', render: (r) => formatCurrency(r.totalValue) },
          ]}
          rows={result}
          rowKey={(r) => r.supplierId}
        />
      );
    }
    if (tab === 'movements') {
      return (
        <div className="flex flex-col gap-4">
          {result.totalQuantityByType ? (
            <div className="flex flex-wrap gap-4 card p-4 text-sm">
              {Object.entries(result.totalQuantityByType).map(([k, v]) => (
                <div key={k}><span className="text-slate-500">{k}:</span> <span className="font-semibold">{v}</span></div>
              ))}
            </div>
          ) : null}
          <Table
            columns={[
              { key: 'timestamp', header: 'When', render: (r) => formatDateTime(r.timestamp) },
              { key: 'productName', header: 'Product' },
              { key: 'movementType', header: 'Type' },
              { key: 'direction', header: 'Dir' },
              { key: 'quantity', header: 'Qty', align: 'right' },
              { key: 'actingUsername', header: 'By' },
            ]}
            rows={result.movements}
            rowKey={(r) => r.id}
          />
        </div>
      );
    }
    if (tab === 'lowstock') {
      return (
        <Table
          columns={[
            { key: 'productName', header: 'Product' },
            { key: 'sku', header: 'SKU' },
            { key: 'categoryName', header: 'Category', render: (r) => r.categoryName || '—' },
            { key: 'currentStock', header: 'Stock', align: 'right' },
            { key: 'maxStockLevel', header: 'Max', align: 'right' },
            { key: 'stockStatus', header: 'Status' },
            { key: 'recommendedReorderQuantity', header: 'Reorder', align: 'right' },
          ]}
          rows={result}
          rowKey={(r) => r.productId}
        />
      );
    }
    return null;
  };

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-semibold text-slate-800">Reports</h1>

      <div className="flex flex-wrap gap-1 border-b border-slate-200">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            onClick={() => switchTab(t.key)}
            className={`-mb-px rounded-t-md border-b-2 px-3 py-2 text-sm font-medium ${
              tab === t.key
                ? 'border-blue-600 text-blue-700'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      <div className="flex flex-wrap items-end gap-3">
        {activeTab.dated ? (
          <>
            <div className="flex min-w-[160px] flex-col gap-1">
              <label className="text-xs font-medium text-slate-600">From</label>
              <input type="date" className={inputClass} value={startDate} onChange={(e) => setStartDate(e.target.value)} />
            </div>
            <div className="flex min-w-[160px] flex-col gap-1">
              <label className="text-xs font-medium text-slate-600">To</label>
              <input type="date" className={inputClass} value={endDate} onChange={(e) => setEndDate(e.target.value)} />
            </div>
          </>
        ) : null}
        {tab === 'movements' ? (
          <div className="flex min-w-[160px] flex-col gap-1">
            <label className="text-xs font-medium text-slate-600">Type</label>
            <select className={inputClass} value={movementType} onChange={(e) => setMovementType(e.target.value)}>
              {MOVEMENT_TYPES.map((t) => <option key={t || 'all'} value={t}>{t || 'All'}</option>)}
            </select>
          </div>
        ) : null}
        <button
          type="button"
          onClick={generate}
          className="btn-primary"
        >
          Generate Report
        </button>
      </div>

      {dateError ? <ErrorMessage error={dateError} /> : null}

      <div>{renderResult()}</div>
    </div>
  );
}
