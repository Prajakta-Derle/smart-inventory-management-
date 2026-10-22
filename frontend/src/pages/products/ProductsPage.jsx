import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Pencil, Ban, Eye } from 'lucide-react';
import productsApi from '../../api/productsApi';
import categoriesApi from '../../api/categoriesApi';
import useToast from '../../hooks/useToast';
import useAuth from '../../hooks/useAuth';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import { formatCurrency } from '../../utils/format';

const EMPTY_FORM = {
  name: '',
  sku: '',
  categoryId: '',
  description: '',
  purchasePrice: '',
  sellingPrice: '',
  minStockLevel: '0',
  maxStockLevel: '0',
  unit: '',
};
const PAGE_SIZE = 10;

export default function ProductsPage() {
  const toast = useToast();
  const { isAdmin } = useAuth();

  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0);
  const [meta, setMeta] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [categories, setCategories] = useState([]);

  // filters + sort
  const [name, setName] = useState('');
  const [sku, setSku] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [activeFilter, setActiveFilter] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [sortDir, setSortDir] = useState('asc');

  // modal
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [confirmTarget, setConfirmTarget] = useState(null);
  const [deactivating, setDeactivating] = useState(false);

  useEffect(() => {
    categoriesApi
      .list()
      .then((data) => setCategories((data || []).filter((c) => c.active)))
      .catch(() => setCategories([]));
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE, sortBy, sortDir };
      if (name.trim()) params.name = name.trim();
      if (sku.trim()) params.sku = sku.trim();
      if (categoryId) params.categoryId = categoryId;
      if (activeFilter !== '') params.active = activeFilter;
      const data = await productsApi.list(params);
      setRows(data.content || []);
      setMeta({ totalPages: data.totalPages, totalElements: data.totalElements });
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [page, sortBy, sortDir, name, sku, categoryId, activeFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFieldErrors({});
    setFormOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({
      name: row.name,
      sku: row.sku,
      categoryId: String(row.categoryId ?? ''),
      description: row.description || '',
      purchasePrice: String(row.purchasePrice),
      sellingPrice: String(row.sellingPrice),
      minStockLevel: String(row.minStockLevel),
      maxStockLevel: String(row.maxStockLevel),
      unit: row.unit || '',
    });
    setFieldErrors({});
    setFormOpen(true);
  };

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Name is required';
    if (!form.sku.trim()) e.sku = 'SKU is required';
    if (!form.categoryId) e.categoryId = 'Category is required';
    if (!form.unit.trim()) e.unit = 'Unit is required';
    const pp = Number(form.purchasePrice);
    const sp = Number(form.sellingPrice);
    const min = Number(form.minStockLevel);
    const max = Number(form.maxStockLevel);
    if (form.purchasePrice === '' || Number.isNaN(pp) || pp < 0) e.purchasePrice = 'Must be 0 or more';
    if (form.sellingPrice === '' || Number.isNaN(sp) || sp < 0) e.sellingPrice = 'Must be 0 or more';
    if (Number.isNaN(min) || min < 0) e.minStockLevel = 'Must be 0 or more';
    if (Number.isNaN(max) || max < 0) e.maxStockLevel = 'Must be 0 or more';
    if (!e.minStockLevel && !e.maxStockLevel && min > max) {
      e.minStockLevel = 'Min stock must not exceed max stock';
    }
    setFieldErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = {
        name: form.name.trim(),
        sku: form.sku.trim(),
        categoryId: Number(form.categoryId),
        description: form.description.trim() || null,
        purchasePrice: Number(form.purchasePrice),
        sellingPrice: Number(form.sellingPrice),
        minStockLevel: Number(form.minStockLevel),
        maxStockLevel: Number(form.maxStockLevel),
        unit: form.unit.trim(),
      };
      if (editing) {
        await productsApi.update(editing.id, payload);
        toast.success('Product updated');
      } else {
        await productsApi.create(payload);
        toast.success('Product created');
      }
      setFormOpen(false);
      await load();
    } catch (err) {
      if (err.fieldErrors) setFieldErrors(err.fieldErrors);
      toast.error(err.message || 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async () => {
    setDeactivating(true);
    try {
      await productsApi.deactivate(confirmTarget.id);
      toast.success('Product deactivated');
      setConfirmTarget(null);
      await load();
    } catch (err) {
      toast.error(err.message || 'Deactivate failed');
    } finally {
      setDeactivating(false);
    }
  };

  const onSortChange = (key) => {
    if (sortBy === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
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
    { key: 'name', header: 'Name', sortable: true },
    { key: 'sku', header: 'SKU', sortable: true },
    { key: 'categoryName', header: 'Category', render: (r) => r.categoryName || '—' },
    { key: 'currentStock', header: 'Stock', align: 'right', sortable: true },
    { key: 'purchasePrice', header: 'Purchase', align: 'right', render: (r) => formatCurrency(r.purchasePrice) },
    { key: 'sellingPrice', header: 'Selling', align: 'right', render: (r) => formatCurrency(r.sellingPrice) },
    {
      key: 'active',
      header: 'Status',
      render: (r) => (
        <span
          className={r.active ? 'badge-green' : 'badge-slate'}
        >
          {r.active ? 'Active' : 'Inactive'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (r) => (
        <div className="flex justify-end gap-2">
          <Link
            to={`/products/${r.id}`}
            className="inline-flex items-center gap-1 rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
          >
            <Eye size={14} /> View
          </Link>
          <button
            type="button"
            onClick={() => openEdit(r)}
            className="inline-flex items-center gap-1 rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
          >
            <Pencil size={14} /> Edit
          </button>
          {isAdmin && r.active ? (
            <button
              type="button"
              onClick={() => setConfirmTarget(r)}
              className="inline-flex items-center gap-1 rounded-md border border-red-300 px-2 py-1 text-xs text-red-600 hover:bg-red-50"
            >
              <Ban size={14} /> Deactivate
            </button>
          ) : null}
        </div>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Products</h1>
        <button
          type="button"
          onClick={openCreate}
          className="btn-primary"
        >
          <Plus size={16} /> Create Product
        </button>
      </div>

      <form onSubmit={onSearchSubmit} className="flex flex-wrap items-end gap-3">
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Name</label>
          <input className={inputClass} value={name} onChange={(e) => setName(e.target.value)} />
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">SKU</label>
          <input className={inputClass} value={sku} onChange={(e) => setSku(e.target.value)} />
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Category</label>
          <select className={inputClass} value={categoryId}
            onChange={(e) => { setCategoryId(e.target.value); setPage(0); }}>
            <option value="">All</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Status</label>
          <select className={inputClass} value={activeFilter}
            onChange={(e) => { setActiveFilter(e.target.value); setPage(0); }}>
            <option value="">All</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
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
        emptyTitle="No products"
        emptyMessage="Create a product or adjust your filters."
      />

      <Modal
        open={formOpen}
        title={editing ? 'Edit Product' : 'Create Product'}
        onClose={() => setFormOpen(false)}
        onConfirm={handleSave}
        confirmLabel={editing ? 'Save changes' : 'Create'}
        busy={saving}
      >
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <FormField label="Name" htmlFor="p-name" required error={fieldErrors.name}>
            <input id="p-name" className={inputClass} value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
          </FormField>
          <FormField label="SKU" htmlFor="p-sku" required error={fieldErrors.sku}>
            <input id="p-sku" className={inputClass} value={form.sku}
              onChange={(e) => setForm((f) => ({ ...f, sku: e.target.value }))} />
          </FormField>
          <FormField label="Category" htmlFor="p-cat" required error={fieldErrors.categoryId}>
            <select id="p-cat" className={inputClass} value={form.categoryId}
              onChange={(e) => setForm((f) => ({ ...f, categoryId: e.target.value }))}>
              <option value="">Select…</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </FormField>
          <FormField label="Unit" htmlFor="p-unit" required error={fieldErrors.unit}>
            <input id="p-unit" className={inputClass} value={form.unit} placeholder="pcs, kg, box…"
              onChange={(e) => setForm((f) => ({ ...f, unit: e.target.value }))} />
          </FormField>
          <FormField label="Purchase price" htmlFor="p-pp" required error={fieldErrors.purchasePrice}>
            <input id="p-pp" type="number" min="0" step="0.01" className={inputClass} value={form.purchasePrice}
              onChange={(e) => setForm((f) => ({ ...f, purchasePrice: e.target.value }))} />
          </FormField>
          <FormField label="Selling price" htmlFor="p-sp" required error={fieldErrors.sellingPrice}>
            <input id="p-sp" type="number" min="0" step="0.01" className={inputClass} value={form.sellingPrice}
              onChange={(e) => setForm((f) => ({ ...f, sellingPrice: e.target.value }))} />
          </FormField>
          <FormField label="Min stock level" htmlFor="p-min" required error={fieldErrors.minStockLevel}>
            <input id="p-min" type="number" min="0" className={inputClass} value={form.minStockLevel}
              onChange={(e) => setForm((f) => ({ ...f, minStockLevel: e.target.value }))} />
          </FormField>
          <FormField label="Max stock level" htmlFor="p-max" required error={fieldErrors.maxStockLevel}>
            <input id="p-max" type="number" min="0" className={inputClass} value={form.maxStockLevel}
              onChange={(e) => setForm((f) => ({ ...f, maxStockLevel: e.target.value }))} />
          </FormField>
          <div className="sm:col-span-2">
            <FormField label="Description" htmlFor="p-desc" error={fieldErrors.description}>
              <textarea id="p-desc" rows={2} className={inputClass} value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
            </FormField>
          </div>
          {editing ? (
            <p className="sm:col-span-2 text-xs text-slate-500">
              Current stock is managed via stock movements and purchase orders — it cannot be edited here.
            </p>
          ) : (
            <p className="sm:col-span-2 text-xs text-slate-500">
              New products start with a current stock of 0.
            </p>
          )}
        </div>
      </Modal>

      <Modal
        open={Boolean(confirmTarget)}
        title="Deactivate product"
        size="sm"
        onClose={() => setConfirmTarget(null)}
        onConfirm={handleDeactivate}
        confirmLabel="Deactivate"
        confirmVariant="danger"
        busy={deactivating}
      >
        <p className="text-sm text-slate-600">
          Deactivate <span className="font-medium">{confirmTarget?.name}</span>? It will be hidden
          from active lists but not deleted.
        </p>
      </Modal>
    </div>
  );
}
