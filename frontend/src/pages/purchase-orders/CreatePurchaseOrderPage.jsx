import { useEffect, useMemo, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Plus, Trash2 } from 'lucide-react';
import purchaseOrdersApi from '../../api/purchaseOrdersApi';
import suppliersApi from '../../api/suppliersApi';
import productsApi from '../../api/productsApi';
import useToast from '../../hooks/useToast';
import FormField from '../../components/FormField';
import ErrorMessage from '../../components/ErrorMessage';
import { formatCurrency } from '../../utils/format';

const todayISO = () => new Date().toISOString().slice(0, 10);
const emptyItem = () => ({ productId: '', quantity: '1', purchasePrice: '' });

export default function CreatePurchaseOrderPage() {
  const navigate = useNavigate();
  const toast = useToast();

  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);

  const [supplierId, setSupplierId] = useState('');
  const [orderDate, setOrderDate] = useState(todayISO());
  const [expectedDeliveryDate, setExpectedDeliveryDate] = useState(todayISO());
  const [items, setItems] = useState([emptyItem()]);

  const [fieldErrors, setFieldErrors] = useState({});
  const [itemErrors, setItemErrors] = useState({});
  const [apiError, setApiError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    suppliersApi
      .list({ page: 0, size: 200, active: 'true' })
      .then((d) => setSuppliers(d.content || []))
      .catch(() => setSuppliers([]));
    productsApi
      .list({ page: 0, size: 500, active: 'true' })
      .then((d) => setProducts(d.content || []))
      .catch(() => setProducts([]));
  }, []);

  const total = useMemo(
    () =>
      items.reduce((sum, it) => {
        const q = Number(it.quantity);
        const p = Number(it.purchasePrice);
        if (!Number.isNaN(q) && !Number.isNaN(p)) return sum + q * p;
        return sum;
      }, 0),
    [items],
  );

  const updateItem = (idx, key, value) =>
    setItems((prev) => prev.map((it, i) => (i === idx ? { ...it, [key]: value } : it)));
  const addItem = () => setItems((prev) => [...prev, emptyItem()]);
  const removeItem = (idx) => setItems((prev) => prev.filter((_, i) => i !== idx));

  const validate = () => {
    const errors = {};
    const iErrors = {};

    if (!supplierId) errors.supplierId = 'Supplier is required';
    if (!orderDate) errors.orderDate = 'Order date is required';
    if (!expectedDeliveryDate) errors.expectedDeliveryDate = 'Expected delivery date is required';
    if (orderDate && expectedDeliveryDate && expectedDeliveryDate < orderDate) {
      errors.expectedDeliveryDate = 'Delivery date must be on or after the order date';
    }
    if (items.length === 0) errors.items = 'At least one item is required';

    const seen = new Set();
    items.forEach((it, idx) => {
      const e = {};
      if (!it.productId) e.productId = 'Required';
      else if (seen.has(it.productId)) e.productId = 'Duplicate product';
      else seen.add(it.productId);
      if (!(Number(it.quantity) > 0)) e.quantity = 'Must be > 0';
      if (!(Number(it.purchasePrice) > 0)) e.purchasePrice = 'Must be > 0';
      if (Object.keys(e).length) iErrors[idx] = e;
    });

    setFieldErrors(errors);
    setItemErrors(iErrors);
    return Object.keys(errors).length === 0 && Object.keys(iErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);
    if (!validate()) return;

    setSubmitting(true);
    try {
      const payload = {
        supplierId: Number(supplierId),
        orderDate,
        expectedDeliveryDate,
        items: items.map((it) => ({
          productId: Number(it.productId),
          quantity: Number(it.quantity),
          purchasePrice: Number(it.purchasePrice),
        })),
      };
      await purchaseOrdersApi.create(payload);
      toast.success('Purchase order created');
      navigate('/purchase-orders', { replace: true });
    } catch (err) {
      setApiError(err);
    } finally {
      setSubmitting(false);
    }
  };

  const inputClass = 'input';
  const cellInput = 'input py-1.5';

  return (
    <div className="flex flex-col gap-4">
      <Link to="/purchase-orders" className="inline-flex items-center gap-1 text-sm text-brand-600 hover:underline">
        <ArrowLeft size={16} /> Back to purchase orders
      </Link>
      <h1 className="text-xl font-semibold text-slate-800">Create Purchase Order</h1>

      {apiError ? <ErrorMessage error={apiError} /> : null}

      <form onSubmit={handleSubmit} className="flex flex-col gap-5" noValidate>
        <div className="grid grid-cols-1 gap-4 card p-5 sm:grid-cols-3">
          <FormField label="Supplier" htmlFor="po-supplier" required error={fieldErrors.supplierId}>
            <select id="po-supplier" className={inputClass} value={supplierId}
              onChange={(e) => setSupplierId(e.target.value)}>
              <option value="">Select…</option>
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>{s.supplierName} — {s.companyName}</option>
              ))}
            </select>
          </FormField>
          <FormField label="Order date" htmlFor="po-order-date" required error={fieldErrors.orderDate}>
            <input id="po-order-date" type="date" className={inputClass} value={orderDate}
              onChange={(e) => setOrderDate(e.target.value)} />
          </FormField>
          <FormField label="Expected delivery" htmlFor="po-delivery" required error={fieldErrors.expectedDeliveryDate}>
            <input id="po-delivery" type="date" className={inputClass} value={expectedDeliveryDate}
              onChange={(e) => setExpectedDeliveryDate(e.target.value)} />
          </FormField>
        </div>

        <div className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-700">Line items</h2>
            <button type="button" onClick={addItem}
              className="inline-flex items-center gap-1 rounded-md border border-slate-300 px-2 py-1 text-sm hover:bg-slate-50">
              <Plus size={14} /> Add item
            </button>
          </div>
          {fieldErrors.items ? <p className="mb-2 text-xs text-red-600">{fieldErrors.items}</p> : null}

          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                  <th className="px-2 py-1">Product</th>
                  <th className="px-2 py-1 w-28">Quantity</th>
                  <th className="px-2 py-1 w-32">Purchase price</th>
                  <th className="px-2 py-1 w-28 text-right">Subtotal</th>
                  <th className="px-2 py-1 w-10"></th>
                </tr>
              </thead>
              <tbody>
                {items.map((it, idx) => {
                  const err = itemErrors[idx] || {};
                  const subtotal = (Number(it.quantity) || 0) * (Number(it.purchasePrice) || 0);
                  return (
                    <tr key={idx} className="align-top">
                      <td className="px-2 py-1">
                        <select className={cellInput} value={it.productId}
                          onChange={(e) => updateItem(idx, 'productId', e.target.value)}>
                          <option value="">Select…</option>
                          {products.map((p) => (
                            <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>
                          ))}
                        </select>
                        {err.productId ? <p className="mt-0.5 text-xs text-red-600">{err.productId}</p> : null}
                      </td>
                      <td className="px-2 py-1">
                        <input type="number" min="1" className={cellInput} value={it.quantity}
                          onChange={(e) => updateItem(idx, 'quantity', e.target.value)} />
                        {err.quantity ? <p className="mt-0.5 text-xs text-red-600">{err.quantity}</p> : null}
                      </td>
                      <td className="px-2 py-1">
                        <input type="number" min="0.01" step="0.01" className={cellInput} value={it.purchasePrice}
                          onChange={(e) => updateItem(idx, 'purchasePrice', e.target.value)} />
                        {err.purchasePrice ? <p className="mt-0.5 text-xs text-red-600">{err.purchasePrice}</p> : null}
                      </td>
                      <td className="px-2 py-1 text-right text-sm text-slate-700">{formatCurrency(subtotal)}</td>
                      <td className="px-2 py-1 text-right">
                        <button type="button" onClick={() => removeItem(idx)}
                          disabled={items.length === 1}
                          className="rounded p-1 text-slate-400 hover:bg-red-50 hover:text-red-600 disabled:opacity-30"
                          aria-label="Remove item">
                          <Trash2 size={16} />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="mt-3 flex justify-end border-t border-slate-200 pt-3 text-sm font-semibold text-slate-800">
            Total: {formatCurrency(total)}
          </div>
        </div>

        <div className="flex justify-end gap-2">
          <Link to="/purchase-orders"
            className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
            Cancel
          </Link>
          <button type="submit" disabled={submitting} className="btn-primary">
            {submitting ? 'Creating…' : 'Create order'}
          </button>
        </div>
      </form>
    </div>
  );
}
