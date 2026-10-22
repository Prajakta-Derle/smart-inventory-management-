/**
 * A single KPI metric card for the dashboard. `index` staggers the entrance.
 */
export default function KpiCard({ icon: Icon, label, value, tone = 'bg-brand-100 text-brand-700', index = 0 }) {
  return (
    <div
      className="card-hover flex animate-fade-up items-center gap-3 px-5 py-4"
      style={{ animationDelay: `${index * 60}ms` }}
    >
      {Icon ? (
        <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${tone}`}>
          <Icon size={20} />
        </div>
      ) : null}
      <div className="min-w-0">
        <div className="truncate text-2xl font-semibold text-slate-800">{value}</div>
        <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</div>
      </div>
    </div>
  );
}
