import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import suppliersApi from '../../api/suppliersApi';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';

function Field({ label, value }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</span>
      <span className="text-sm text-slate-800">{value || '—'}</span>
    </div>
  );
}

export default function SupplierDetailPage() {
  const { id } = useParams();
  const [supplier, setSupplier] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await suppliersApi.getById(id);
        if (active) setSupplier(data);
      } catch (err) {
        if (active) setError(err);
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [id]);

  return (
    <div className="flex flex-col gap-4">
      <Link to="/suppliers" className="inline-flex items-center gap-1 text-sm text-brand-600 hover:underline">
        <ArrowLeft size={16} /> Back to suppliers
      </Link>

      {loading ? <LoadingSpinner /> : null}
      {error ? <ErrorMessage error={error} /> : null}

      {supplier ? (
        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-semibold text-slate-800">{supplier.supplierName}</h1>
            <span
              className={supplier.active ? 'badge-green' : 'badge-slate'}
            >
              {supplier.active ? 'Active' : 'Inactive'}
            </span>
          </div>

          <div className="grid grid-cols-1 gap-4 card p-5 sm:grid-cols-2">
            <Field label="Company" value={supplier.companyName} />
            <Field label="Email" value={supplier.email} />
            <Field label="Phone" value={supplier.phone} />
            <Field label="GST number" value={supplier.gstNumber} />
            <Field label="Total purchase orders" value={String(supplier.totalPurchaseOrderCount)} />
            <div className="sm:col-span-2">
              <Field label="Address" value={supplier.address} />
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
