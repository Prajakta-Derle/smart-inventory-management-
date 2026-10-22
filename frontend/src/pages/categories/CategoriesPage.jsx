import { useCallback, useEffect, useState } from 'react';
import { Plus, Pencil, Ban } from 'lucide-react';
import categoriesApi from '../../api/categoriesApi';
import useToast from '../../hooks/useToast';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

const EMPTY_FORM = { name: '', description: '' };

export default function CategoriesPage() {
  const toast = useToast();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Create/Edit modal state
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null); // null = create
  const [form, setForm] = useState(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);

  // Deactivate confirm state
  const [confirmTarget, setConfirmTarget] = useState(null);
  const [deactivating, setDeactivating] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await categoriesApi.list();
      setRows(data || []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

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
    setForm({ name: row.name, description: row.description || '' });
    setFieldErrors({});
    setFormOpen(true);
  };

  const validate = () => {
    const errors = {};
    if (!form.name.trim()) errors.name = 'Name is required';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = { name: form.name.trim(), description: form.description.trim() || null };
      if (editing) {
        await categoriesApi.update(editing.id, payload);
        toast.success('Category updated');
      } else {
        await categoriesApi.create(payload);
        toast.success('Category created');
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
      await categoriesApi.deactivate(confirmTarget.id);
      toast.success('Category deactivated');
      setConfirmTarget(null);
      await load();
    } catch (err) {
      toast.error(err.message || 'Deactivate failed');
    } finally {
      setDeactivating(false);
    }
  };

  const inputClass = 'input';

  const columns = [
    { key: 'name', header: 'Name' },
    { key: 'description', header: 'Description', render: (r) => r.description || '—' },
    { key: 'productCount', header: 'Products', align: 'right' },
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
        <h1 className="text-xl font-semibold text-slate-800">Categories</h1>
        <button
          type="button"
          onClick={openCreate}
          className="btn-primary"
        >
          <Plus size={16} /> Create Category
        </button>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        loading={loading}
        error={error}
        emptyTitle="No categories"
        emptyMessage="Create your first category to get started."
      />

      {/* Create / Edit modal */}
      <Modal
        open={formOpen}
        title={editing ? 'Edit Category' : 'Create Category'}
        onClose={() => setFormOpen(false)}
        onConfirm={handleSave}
        confirmLabel={editing ? 'Save changes' : 'Create'}
        busy={saving}
      >
        <div className="flex flex-col gap-4">
          <FormField label="Name" htmlFor="cat-name" required error={fieldErrors.name}>
            <input
              id="cat-name"
              className={inputClass}
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
            />
          </FormField>
          <FormField label="Description" htmlFor="cat-desc" error={fieldErrors.description}>
            <textarea
              id="cat-desc"
              rows={3}
              className={inputClass}
              value={form.description}
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            />
          </FormField>
        </div>
      </Modal>

      {/* Deactivate confirm */}
      <Modal
        open={Boolean(confirmTarget)}
        title="Deactivate category"
        size="sm"
        onClose={() => setConfirmTarget(null)}
        onConfirm={handleDeactivate}
        confirmLabel="Deactivate"
        confirmVariant="danger"
        busy={deactivating}
      >
        <p className="text-sm text-slate-600">
          Deactivate <span className="font-medium">{confirmTarget?.name}</span>? It will be
          hidden from active lists but not deleted.
        </p>
      </Modal>
    </div>
  );
}
