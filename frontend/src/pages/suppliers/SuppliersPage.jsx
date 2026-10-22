import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Pencil, Ban, Eye } from 'lucide-react';
import suppliersApi from '../../api/suppliersApi';
import useToast from '../../hooks/useToast';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

const EMPTY_FORM = {
  supplierName: '',
  companyName: '',
  email: '',
  phone: '',
  address: '',
  gstNumber: '',
};
const PAGE_SIZE = 10;
const PHONE_RE = /^[+]?[0-9][0-9\s-]{6,19}$/;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function SuppliersPage() {
  const toast = useToast();

  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0);
  const [meta, setMeta] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // filters
  const [search, setSearch] = useState('');
  const [activeFilter, setActiveFilter] = useState('');

  // modal
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [confirmTarget, setConfirmTarget] = useState(null);
  const [deactivating, setDeactivating] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE };
      if (search.trim()) {
        params.supplierName = search.trim();
        params.companyName = search.trim();
      }
      if (activeFilter !== '') params.active = activeFilter;
      const data = await suppliersApi.list(params);
      setRows(data.content || []);
      setMeta({ totalPages: data.totalPages, totalElements: data.totalElements });
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [page, search, activeFilter]);

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
      supplierName: row.supplierName || '',
      companyName: row.companyName || '',
      email: row.email || '',
      phone: row.phone || '',
      address: row.address || '',
      gstNumber: row.gstNumber || '',
    });
    setFieldErrors({});
    setFormOpen(true);
  };

  const validate = () => {
    const e = {};
    if (!form.supplierName.trim()) e.supplierName = 'Supplier name is required';
    if (!form.companyName.trim()) e.companyName = 'Company name is required';
    if (!EMAIL_RE.test(form.email.trim())) e.email = 'Enter a valid email';
    if (form.phone && !PHONE_RE.test(form.phone.trim())) e.phone = 'Enter a valid phone number';
    setFieldErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = {
        supplierName: form.supplierName.trim(),
        companyName: form.companyName.trim(),
        email: form.email.trim(),
        phone: form.phone.trim() || null,
        address: form.address.trim() || null,
        gstNumber: form.gstNumber.trim() || null,
      };
      if (editing) {
        await suppliersApi.update(editing.id, payload);
        toast.success('Supplier updated');
      } else {
        await suppliersApi.create(payload);
        toast.success('Supplier created');
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
      await suppliersApi.deactivate(confirmTarget.id);
      toast.success('Supplier deactivated');
      setConfirmTarget(null);
      await load();
    } catch (err) {
      toast.error(err.message || 'Deactivate failed');
    } finally {
      setDeactivating(false);
    }
  };

  const onSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    load();
  };

  const inputClass = 'input';

  const columns = [
    { key: 'supplierName', header: 'Supplier' },
    { key: 'companyName', header: 'Company' },
    { key: 'email', header: 'Email' },
    { key: 'phone', header: 'Phone', render: (r) => r.phone || '—' },
    { key: 'totalPurchaseOrderCount', header: 'POs', align: 'right' },
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
            to={`/suppliers/${r.id}`}
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
          {r.active ? (
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
        <h1 className="text-xl font-semibold text-slate-800">Suppliers</h1>
        <button
          type="button"
          onClick={openCreate}
          className="btn-primary"
        >
          <Plus size={16} /> Create Supplier
        </button>
      </div>

      <form onSubmit={onSearchSubmit} className="flex flex-wrap items-end gap-3">
        <div className="flex min-w-[220px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Search (name / company)</label>
          <input
            className={inputClass}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Type to search…"
          />
        </div>
        <div className="flex min-w-[160px] flex-col gap-1">
          <label className="text-xs font-medium text-slate-600">Status</label>
          <select
            className={inputClass}
            value={activeFilter}
            onChange={(e) => {
              setActiveFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
        </div>
        <button
          type="submit"
          className="rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
        >
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
        emptyTitle="No suppliers"
        emptyMessage="Create a supplier or adjust your filters."
      />

      <Modal
        open={formOpen}
        title={editing ? 'Edit Supplier' : 'Create Supplier'}
        onClose={() => setFormOpen(false)}
        onConfirm={handleSave}
        confirmLabel={editing ? 'Save changes' : 'Create'}
        busy={saving}
      >
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <FormField label="Supplier name" htmlFor="s-name" required error={fieldErrors.supplierName}>
            <input id="s-name" className={inputClass} value={form.supplierName}
              onChange={(e) => setForm((f) => ({ ...f, supplierName: e.target.value }))} />
          </FormField>
          <FormField label="Company name" htmlFor="s-company" required error={fieldErrors.companyName}>
            <input id="s-company" className={inputClass} value={form.companyName}
              onChange={(e) => setForm((f) => ({ ...f, companyName: e.target.value }))} />
          </FormField>
          <FormField label="Email" htmlFor="s-email" required error={fieldErrors.email}>
            <input id="s-email" type="email" className={inputClass} value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} />
          </FormField>
          <FormField label="Phone" htmlFor="s-phone" error={fieldErrors.phone}>
            <input id="s-phone" className={inputClass} value={form.phone}
              onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} />
          </FormField>
          <FormField label="GST number" htmlFor="s-gst" error={fieldErrors.gstNumber}>
            <input id="s-gst" className={inputClass} value={form.gstNumber}
              onChange={(e) => setForm((f) => ({ ...f, gstNumber: e.target.value }))} />
          </FormField>
          <div className="sm:col-span-2">
            <FormField label="Address" htmlFor="s-address" error={fieldErrors.address}>
              <textarea id="s-address" rows={2} className={inputClass} value={form.address}
                onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} />
            </FormField>
          </div>
        </div>
      </Modal>

      <Modal
        open={Boolean(confirmTarget)}
        title="Deactivate supplier"
        size="sm"
        onClose={() => setConfirmTarget(null)}
        onConfirm={handleDeactivate}
        confirmLabel="Deactivate"
        confirmVariant="danger"
        busy={deactivating}
      >
        <p className="text-sm text-slate-600">
          Deactivate <span className="font-medium">{confirmTarget?.supplierName}</span>? It will
          be hidden from active lists but not deleted.
        </p>
      </Modal>
    </div>
  );
}
