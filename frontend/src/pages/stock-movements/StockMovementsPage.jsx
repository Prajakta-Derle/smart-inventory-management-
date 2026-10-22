import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Plus } from 'lucide-react';
import stockMovementsApi from '../../api/stockMovementsApi';
import productsApi from '../../api/productsApi';
import useToast from '../../hooks/useToast';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import { formatCurrency, formatDateTime } from '../../utils/format';

const PAGE_SIZE = 10;
const TYPES = ['PURCHASE', 'SALE', 'DAMAGED', 'ADJUSTMENT'];

const DIR_STYLE = {
  ADD: 'bg-green-100 text-green-700',
  REDUCE: 'bg-red-100 text-red-700',
};

const emptyForm = () => ({
  productId: '',
  movementType: 'SALE',
  quantity: '1',
  direction: 'ADD',
  note: '',
});

export default function StockMovementsPage() {
  const toast = useToast();
  const [searchParams] = useSearchParams();

  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0);
  const [meta, setMeta] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [products, setProducts] = useState([]);

  // filters
  const [productId, setProductId] = useState(searchParams.get('productId') || '');
  const [movementType, setMovementType] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  // record modal
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(emptyForm());
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    productsApi
      .list({ page: 0, size: 500, active: 'true' })
      .then((d) => setProducts(d.content || []))
      .catch(() => setProducts([]));
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE };
      if (productId) params.productId = productId;
      if (movementType) params.movementType = movementType;
      if (startDate) params.startDate = `${startDate}T00:00:00`;
      if (endDate) params.endDate = `${endDate}T23:59:59`;
      const data = await stockMovementsApi.list(params);
      setRows(data.content || []);
      setMeta({ totalPages: data.totalPages, totalElements: data.totalElements });
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [page, productId, movementType, startDate, endDate]);

  useEffect(() => {
    load();
  }, [load]);

  const openRecord = () => {
    setForm({ ...emptyForm(), productId: productId || '' });
    setFieldErrors({});
    setFormOpen(true);
  };

  const isAdjustment = form.movementType === 'ADJUSTMENT';

  const validate = () => {
    const e = {};
    if (!form.productId) e.productId = 'Product is required';
    const q = Number(form.quantity);
    if (!(q >= 1) || q > 1_000_000) e.quantity = 'Quantity must be between 1 and 1,000,000';
    if (isAdjustment && !form.direction) e.direction = 'Direction is required for adjustments';
    if (form.note && form.note.length > 500) e.note = 'Note must be at most 500 characters';
    setFieldErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = {
        productId: Number(form.productId),
        movementType: form.movementType,
        quantity: Number(form.quantity),
        note: form.note.trim() || null,
      };
      // Direction is only sent for ADJUSTMENT; derived server-side otherwise.
      if (isAdjustment) payload.direction = form.direction;
      await stockMovementsApi.record(payload);
      toast.success('Stock movement recorded');
      setFormOpen(false);
      setPage(0);
      await load();
    } catch (err) {
      if (err.fieldErrors) setFieldErrors(err.fieldErrors);
      toast.error(err.message || 'Could not record movement');
    } finally {
      setSaving(false);
    }
  };

  const onSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    load();
  };

  const inputClass = 'input';

  const columns = [
    { key: 'timestamp', header: 'When', render: (r) => formatDateTime(r.timestamp) },
    { key: 'productName', header: 'Product' },
    { key: 'sku', header: 'SKU' },
    { key: 'movementType', header: 'Type' },
    {
      key: 'direction',
      header: 'Dir',
      render: (r) => (
        <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${DIR_STYLE[r.direction] || ''}`}>
          {r.direction}
        </span>
      ),
    },
    { key: 'quantity', header: 'Qty', align: 'right' },
    { key: 'stockBefore', header: 'Before', align: 'right' },
    { key: 'stockAfter', header: 'After', align: 'right' },
    { key: 'unitCost', header: 'Unit cost', align: 'right', render: (r) => formatCurrency(r.unitCost) },
    { key: 'actingUsername', header: 'By', render: (r) => r.actingUsername || '—' },
  ];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Stock Movements</h1>
        <button type="button" onClick={openRecord} className="btn-primary">
          <Plus size={16} /> Record Movement
        </button>
      </div>

      <form onSubmit={onSearchSubmit} className="flex flex-wrap items-end gap-3">
        <div className="flex min-w-[180px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Product</label>
          <select className={inputClass} value={productId}
            onChange={(e) => { setProductId(e.target.value); setPage(0); }}>
            <option value="">All</option>
            {products.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}
          </select>
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Type</label>
          <select className={inputClass} value={movementType}
            onChange={(e) => { setMovementType(e.target.value); setPage(0); }}>
            <option value="">All</option>
            {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">From</label>
          <input type="date" className={inputClass} value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">To</label>
          <input type="date" className={inputClass} value={endDate} onChange={(e) => setEndDate(e.target.value)} />
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
        emptyTitle="No stock movements"
        emptyMessage="Record a movement or adjust your filters."
      />

      <Modal
        open={formOpen}
        title="Record Stock Movement"
        onClose={() => setFormOpen(false)}
        onConfirm={handleSave}
        confirmLabel="Record"
        busy={saving}
      >
        <div className="flex flex-col gap-4">
          <FormField label="Product" htmlFor="m-product" required error={fieldErrors.productId}>
            <select id="m-product" className={inputClass} value={form.productId}
              onChange={(e) => setForm((f) => ({ ...f, productId: e.target.value }))}>
              <option value="">Select…</option>
              {products.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}
            </select>
          </FormField>

          <FormField label="Movement type" htmlFor="m-type" required>
            <select id="m-type" className={inputClass} value={form.movementType}
              onChange={(e) => setForm((f) => ({ ...f, movementType: e.target.value }))}>
              {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </FormField>

          {/* Direction only shown/required for ADJUSTMENT */}
          {isAdjustment ? (
            <FormField label="Direction" htmlFor="m-dir" required error={fieldErrors.direction}>
              <select id="m-dir" className={inputClass} value={form.direction}
                onChange={(e) => setForm((f) => ({ ...f, direction: e.target.value }))}>
                <option value="ADD">ADD (increase stock)</option>
                <option value="REDUCE">REDUCE (decrease stock)</option>
              </select>
            </FormField>
          ) : (
            <p className="text-xs text-slate-500">
              {form.movementType === 'PURCHASE'
                ? 'PURCHASE increases stock.'
                : 'This movement type decreases stock.'}
            </p>
          )}

          <FormField label="Quantity" htmlFor="m-qty" required error={fieldErrors.quantity}>
            <input id="m-qty" type="number" min="1" max="1000000" className={inputClass} value={form.quantity}
              onChange={(e) => setForm((f) => ({ ...f, quantity: e.target.value }))} />
          </FormField>

          <FormField label="Note" htmlFor="m-note" error={fieldErrors.note}>
            <textarea id="m-note" rows={2} className={inputClass} value={form.note}
              onChange={(e) => setForm((f) => ({ ...f, note: e.target.value }))} />
          </FormField>
        </div>
      </Modal>
    </div>
  );
}
