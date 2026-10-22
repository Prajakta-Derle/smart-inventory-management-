import { useCallback, useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import purchaseOrdersApi from '../../api/purchaseOrdersApi';
import useToast from '../../hooks/useToast';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import Modal from '../../components/Modal';
import { formatCurrency, formatDate } from '../../utils/format';
import { StatusBadge } from './PurchaseOrdersPage';

// Allowed transitions per the backend state machine.
const NEXT = {
  DRAFT: ['ORDERED', 'CANCELLED'],
  ORDERED: ['RECEIVED', 'CANCELLED'],
  RECEIVED: [],
  CANCELLED: [],
};

function Field({ label, value }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</span>
      <span className="text-sm text-slate-800">{value ?? '—'}</span>
    </div>
  );
}

export default function PurchaseOrderDetailPage() {
  const { id } = useParams();
  const toast = useToast();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [pendingStatus, setPendingStatus] = useState(null);
  const [working, setWorking] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await purchaseOrdersApi.getById(id);
      setOrder(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const applyStatus = async () => {
    setWorking(true);
    try {
      await purchaseOrdersApi.updateStatus(id, pendingStatus);
      toast.success(`Order marked ${pendingStatus}`);
      setPendingStatus(null);
      await load();
    } catch (err) {
      toast.error(err.message || 'Status update failed');
    } finally {
      setWorking(false);
    }
  };

  const buttonStyle = (target) =>
    target === 'CANCELLED'
      ? 'border-red-300 text-red-600 hover:bg-red-50'
      : target === 'RECEIVED'
        ? 'border-green-300 text-green-700 hover:bg-green-50'
        : 'border-blue-300 text-blue-700 hover:bg-blue-50';

  return (
    <div className="flex flex-col gap-4">
      <Link to="/purchase-orders" className="inline-flex items-center gap-1 text-sm text-brand-600 hover:underline">
        <ArrowLeft size={16} /> Back to purchase orders
      </Link>

      {loading ? <LoadingSpinner /> : null}
      {error ? <ErrorMessage error={error} /> : null}

      {order ? (
        <div className="flex flex-col gap-5">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-xl font-semibold text-slate-800">{order.orderNumber}</h1>
            <StatusBadge status={order.status} />
            <div className="ml-auto flex gap-2">
              {NEXT[order.status]?.map((target) => (
                <button
                  key={target}
                  type="button"
                  onClick={() => setPendingStatus(target)}
                  className={`rounded-md border px-3 py-1.5 text-sm font-medium ${buttonStyle(target)}`}
                >
                  Mark {target}
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 card p-5 sm:grid-cols-2 lg:grid-cols-4">
            <Field label="Supplier" value={order.supplierName} />
            <Field label="Order date" value={formatDate(order.orderDate)} />
            <Field label="Expected delivery" value={formatDate(order.expectedDeliveryDate)} />
            <Field label="Total" value={formatCurrency(order.total)} />
          </div>

          <div>
            <h2 className="mb-2 text-sm font-semibold text-slate-700">Line items</h2>
            <div className="overflow-x-auto rounded-lg border border-slate-200">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Product</th>
                    <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">SKU</th>
                    <th className="px-4 py-2.5 text-right text-xs font-semibold uppercase tracking-wide text-slate-600">Qty</th>
                    <th className="px-4 py-2.5 text-right text-xs font-semibold uppercase tracking-wide text-slate-600">Price</th>
                    <th className="px-4 py-2.5 text-right text-xs font-semibold uppercase tracking-wide text-slate-600">Subtotal</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {(order.items || []).map((it) => (
                    <tr key={it.id}>
                      <td className="px-4 py-2.5 text-sm text-slate-700">{it.productName}</td>
                      <td className="px-4 py-2.5 text-sm text-slate-700">{it.sku}</td>
                      <td className="px-4 py-2.5 text-right text-sm text-slate-700">{it.quantity}</td>
                      <td className="px-4 py-2.5 text-right text-sm text-slate-700">{formatCurrency(it.purchasePrice)}</td>
                      <td className="px-4 py-2.5 text-right text-sm text-slate-700">{formatCurrency(it.subtotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ) : null}

      <Modal
        open={Boolean(pendingStatus)}
        title={`Mark order as ${pendingStatus}`}
        size="sm"
        onClose={() => setPendingStatus(null)}
        onConfirm={applyStatus}
        confirmLabel={`Mark ${pendingStatus}`}
        confirmVariant={pendingStatus === 'CANCELLED' ? 'danger' : 'primary'}
        busy={working}
      >
        {pendingStatus === 'RECEIVED' ? (
          <p className="text-sm text-slate-600">
            Marking this order <span className="font-medium">RECEIVED</span> will{' '}
            <span className="font-medium text-green-700">increase stock</span> for every line item
            and create permanent stock-movement records. This cannot be undone.
          </p>
        ) : pendingStatus === 'CANCELLED' ? (
          <p className="text-sm text-slate-600">
            Cancel this order? Stock is not affected, and a cancelled order cannot be reactivated.
          </p>
        ) : (
          <p className="text-sm text-slate-600">
            Change this order’s status to <span className="font-medium">{pendingStatus}</span>?
          </p>
        )}
      </Modal>
    </div>
  );
}
