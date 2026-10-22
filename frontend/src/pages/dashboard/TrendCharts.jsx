import { useCallback, useEffect, useState } from 'react';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
} from 'recharts';
import dashboardApi from '../../api/dashboardApi';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';
import EmptyState from '../../components/EmptyState';
import { formatCurrency } from '../../utils/format';

const isoDaysAgo = (days) => {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d.toISOString().slice(0, 10);
};
const todayISO = () => new Date().toISOString().slice(0, 10);

function DateRange({ start, end, onStart, onEnd }) {
  const input = 'input py-1.5';
  return (
    <div className="flex items-center gap-2">
      <input type="date" className={input} value={start} onChange={(e) => onStart(e.target.value)} />
      <span className="text-slate-400">–</span>
      <input type="date" className={input} value={end} onChange={(e) => onEnd(e.target.value)} />
    </div>
  );
}

function ChartCard({ title, children, start, end, onStart, onEnd }) {
  return (
    <div className="card p-6">
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-sm font-semibold text-slate-700">{title}</h2>
        <DateRange start={start} end={end} onStart={onStart} onEnd={onEnd} />
      </div>
      <div className="h-64 pt-1">{children}</div>
    </div>
  );
}

export default function TrendCharts() {
  const [start, setStart] = useState(isoDaysAgo(30));
  const [end, setEnd] = useState(todayISO());

  const [purchase, setPurchase] = useState({ loading: true, error: null, data: [] });
  const [inventory, setInventory] = useState({ loading: true, error: null, data: [] });

  const loadPurchase = useCallback(async () => {
    if (!start || !end || start > end) return;
    setPurchase((s) => ({ ...s, loading: true, error: null }));
    try {
      const data = await dashboardApi.purchaseTrend(start, end);
      setPurchase({ loading: false, error: null, data: data || [] });
    } catch (err) {
      setPurchase({ loading: false, error: err, data: [] });
    }
  }, [start, end]);

  const loadInventory = useCallback(async () => {
    if (!start || !end || start > end) return;
    setInventory((s) => ({ ...s, loading: true, error: null }));
    try {
      const data = await dashboardApi.inventoryTrend(start, end);
      setInventory({ loading: false, error: null, data: data || [] });
    } catch (err) {
      setInventory({ loading: false, error: err, data: [] });
    }
  }, [start, end]);

  useEffect(() => {
    loadPurchase();
    loadInventory();
  }, [loadPurchase, loadInventory]);

  const rangeInvalid = start && end && start > end;

  return (
    <div className="flex flex-col gap-6">
      <ChartCard title="Purchase trend" start={start} end={end} onStart={setStart} onEnd={setEnd}>
        {rangeInvalid ? (
          <ErrorMessage error="Start date must be on or before end date" />
        ) : purchase.loading ? (
          <LoadingSpinner />
        ) : purchase.error ? (
          <ErrorMessage error={purchase.error} />
        ) : purchase.data.length === 0 ? (
          <EmptyState title="No purchase data" message="No orders in this range." />
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={purchase.data} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip formatter={(v) => formatCurrency(v)} />
              <Bar dataKey="totalAmount" name="Total" fill="#2563eb" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </ChartCard>

      <ChartCard title="Inventory value trend" start={start} end={end} onStart={setStart} onEnd={setEnd}>
        {rangeInvalid ? (
          <ErrorMessage error="Start date must be on or before end date" />
        ) : inventory.loading ? (
          <LoadingSpinner />
        ) : inventory.error ? (
          <ErrorMessage error={inventory.error} />
        ) : inventory.data.length === 0 ? (
          <EmptyState title="No inventory data" message="No movements in this range." />
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={inventory.data} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip formatter={(v) => formatCurrency(v)} />
              <Line
                type="monotone"
                dataKey="inventoryValue"
                name="Inventory value"
                stroke="#16a34a"
                strokeWidth={2}
                dot={false}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </ChartCard>
    </div>
  );
}
