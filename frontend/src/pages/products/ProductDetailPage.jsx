import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import productsApi from '../../api/productsApi';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import { formatCurrency } from '../../utils/format';

function Field({ label, value }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</span>
      <span className="text-sm text-slate-800">{value ?? '—'}</span>
    </div>
  );
}

export default function ProductDetailPage() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await productsApi.getById(id);
        if (active) setProduct(data);
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
      <Link to="/products" className="inline-flex items-center gap-1 text-sm text-brand-600 hover:underline">
        <ArrowLeft size={16} /> Back to products
      </Link>

      {loading ? <LoadingSpinner /> : null}
      {error ? <ErrorMessage error={error} /> : null}

      {product ? (
        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-semibold text-slate-800">{product.name}</h1>
            <span
              className={product.active ? 'badge-green' : 'badge-slate'}
            >
              {product.active ? 'Active' : 'Inactive'}
            </span>
          </div>

          <div className="grid grid-cols-1 gap-4 card p-5 sm:grid-cols-2">
            <Field label="SKU" value={product.sku} />
            <Field label="Category" value={product.categoryName} />
            <Field label="Current stock" value={product.currentStock} />
            <Field label="Unit" value={product.unit} />
            <Field label="Purchase price" value={formatCurrency(product.purchasePrice)} />
            <Field label="Selling price" value={formatCurrency(product.sellingPrice)} />
            <Field label="Min stock level" value={product.minStockLevel} />
            <Field label="Max stock level" value={product.maxStockLevel} />
            <div className="sm:col-span-2">
              <Field label="Description" value={product.description} />
            </div>
          </div>

          <Link
            to={`/stock-movements?productId=${product.id}`}
            className="text-sm text-brand-600 hover:underline"
          >
            View stock movement history →
          </Link>
        </div>
      ) : null}
    </div>
  );
}
