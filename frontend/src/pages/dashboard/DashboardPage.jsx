import { useEffect, useState } from 'react';
import {
  Package,
  Truck,
  Wallet,
  ClipboardList,
  Clock,
  AlertTriangle,
  PackageX,
} from 'lucide-react';
import dashboardApi from '../../api/dashboardApi';
import lowStockApi from '../../api/lowStockApi';
import useToast from '../../hooks/useToast';
import LoadingSpinner from '../../components/LoadingSpinner';
import { formatCurrency } from '../../utils/format';
import KpiCard from './KpiCard';
import LowStockPanel from './LowStockPanel';
import TrendCharts from './TrendCharts';
import RecentMovements from './RecentMovements';

export default function DashboardPage() {
  const toast = useToast();
  const [summary, setSummary] = useState(null);
  const [critical, setCritical] = useState([]);
  const [movements, setMovements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    (async () => {
      setLoading(true);
      try {
        const [sum, low, recent] = await Promise.all([
          dashboardApi.summary(),
          lowStockApi.summary(),
          dashboardApi.recentMovements(),
        ]);
        if (!active) return;
        setSummary(sum);
        setCritical(low?.topCritical || []);
        setMovements(recent || []);
      } catch (err) {
        if (active) toast.error(err.message || 'Could not load dashboard');
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col gap-4">
        <h1 className="text-xl font-semibold text-slate-800">Dashboard</h1>
        <LoadingSpinner />
      </div>
    );
  }

  const s = summary || {};

  return (
    <div className="flex flex-col gap-5">
      <h1 className="text-xl font-semibold text-slate-800">Dashboard</h1>

      {/* KPI cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard index={0} icon={Package} label="Active products" value={s.totalActiveProducts ?? 0} />
        <KpiCard index={1} icon={Truck} label="Active suppliers" value={s.totalActiveSuppliers ?? 0} tone="bg-indigo-100 text-indigo-700" />
        <KpiCard index={2} icon={Wallet} label="Inventory value" value={formatCurrency(s.totalInventoryValue)} tone="bg-emerald-100 text-emerald-700" />
        <KpiCard index={3} icon={ClipboardList} label="Total POs" value={s.totalPurchaseOrders ?? 0} tone="bg-slate-100 text-slate-700" />
        <KpiCard index={4} icon={Clock} label="Pending (ORDERED)" value={s.orderedPurchaseOrders ?? 0} tone="bg-blue-100 text-blue-700" />
        <KpiCard index={5} icon={AlertTriangle} label="Low stock" value={s.lowStockCount ?? 0} tone="bg-amber-100 text-amber-700" />
        <KpiCard index={6} icon={PackageX} label="Out of stock" value={s.outOfStockCount ?? 0} tone="bg-red-100 text-red-700" />
      </div>

      {/* Charts + alert panel */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <TrendCharts />
        </div>
        <div>
          <LowStockPanel items={critical} />
        </div>
      </div>

      {/* Recent movements */}
      <RecentMovements movements={movements} />
    </div>
  );
}
