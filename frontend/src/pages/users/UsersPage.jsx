import { useCallback, useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import usersApi from '../../api/usersApi';
import useToast from '../../hooks/useToast';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import { formatDateTime } from '../../utils/format';

const PAGE_SIZE = 10;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
const emptyForm = () => ({ username: '', email: '', password: '', role: 'STAFF' });

export default function UsersPage() {
  const toast = useToast();

  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0);
  const [meta, setMeta] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(emptyForm());
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await usersApi.list({ page, size: PAGE_SIZE });
      setRows(data.content || []);
      setMeta({ totalPages: data.totalPages, totalElements: data.totalElements });
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const openCreate = () => {
    setForm(emptyForm());
    setFieldErrors({});
    setFormOpen(true);
  };

  const validate = () => {
    const e = {};
    if (!form.username.trim() || form.username.trim().length < 3) e.username = 'At least 3 characters';
    if (!EMAIL_RE.test(form.email.trim())) e.email = 'Enter a valid email';
    if (!PASSWORD_RE.test(form.password)) e.password = '8+ chars with upper, lower, and a digit';
    if (!['ADMIN', 'STAFF'].includes(form.role)) e.role = 'Select a role';
    setFieldErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      await usersApi.create({
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        role: form.role,
      });
      toast.success(`${form.role} account created`);
      setFormOpen(false);
      setPage(0);
      await load();
    } catch (err) {
      if (err.fieldErrors) setFieldErrors(err.fieldErrors);
      toast.error(err.message || 'Could not create user');
    } finally {
      setSaving(false);
    }
  };

  const inputClass = 'input';

  const columns = [
    { key: 'username', header: 'Username' },
    { key: 'email', header: 'Email' },
    {
      key: 'role',
      header: 'Role',
      render: (r) => (
        <span
          className={r.role === 'ADMIN' ? 'badge-brand' : 'badge-slate'}
        >
          {r.role}
        </span>
      ),
    },
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
    { key: 'createdAt', header: 'Created', render: (r) => formatDateTime(r.createdAt) },
  ];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Users</h1>
        <button
          type="button"
          onClick={openCreate}
          className="btn-primary"
        >
          <Plus size={16} /> Create User
        </button>
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
        emptyTitle="No users"
      />

      <Modal
        open={formOpen}
        title="Create User"
        onClose={() => setFormOpen(false)}
        onConfirm={handleSave}
        confirmLabel="Create"
        busy={saving}
      >
        <div className="flex flex-col gap-4">
          <FormField label="Username" htmlFor="u-name" required error={fieldErrors.username}>
            <input id="u-name" className={inputClass} value={form.username}
              onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))} />
          </FormField>
          <FormField label="Email" htmlFor="u-email" required error={fieldErrors.email}>
            <input id="u-email" type="email" className={inputClass} value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} />
          </FormField>
          <FormField label="Password" htmlFor="u-pass" required error={fieldErrors.password}
            hint="At least 8 characters, with upper, lower, and a digit.">
            <input id="u-pass" type="password" className={inputClass} value={form.password}
              onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))} />
          </FormField>
          <FormField label="Role" htmlFor="u-role" required error={fieldErrors.role}>
            <select id="u-role" className={inputClass} value={form.role}
              onChange={(e) => setForm((f) => ({ ...f, role: e.target.value }))}>
              <option value="STAFF">STAFF</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </FormField>
        </div>
      </Modal>
    </div>
  );
}
