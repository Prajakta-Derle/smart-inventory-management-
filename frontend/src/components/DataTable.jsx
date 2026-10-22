import { ChevronLeft, ChevronRight, ArrowUp, ArrowDown } from 'lucide-react';
import LoadingSpinner from './LoadingSpinner';
import EmptyState from './EmptyState';
import ErrorMessage from './ErrorMessage';

/**
 * Generic table with server-side pagination and optional sorting.
 *
 * Props:
 * - columns: [{ key, header, render?(row), sortable?, align? }]
 * - rows: array of row objects
 * - loading, error
 * - page (0-based), totalPages, totalElements, onPageChange(nextPage)
 * - sortBy, sortDir ('asc'|'desc'), onSortChange(key)
 * - rowKey(row) -> unique key
 */
export function DataTable({
  columns,
  rows = [],
  loading = false,
  error = null,
  page = 0,
  totalPages = 0,
  totalElements = 0,
  onPageChange,
  sortBy,
  sortDir,
  onSortChange,
  rowKey = (row) => row.id,
  emptyTitle,
  emptyMessage,
}) {
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage error={error} className="my-4" />;

  const alignClass = (align) =>
    align === 'right' ? 'text-right' : align === 'center' ? 'text-center' : 'text-left';

  return (
    <div className="flex flex-col gap-3">
      <div className="card overflow-x-auto p-1">
        <table className="min-w-full">
          <thead>
            <tr>
              {columns.map((col) => {
                const isSorted = sortBy === col.key;
                const sortable = col.sortable && onSortChange;
                return (
                  <th
                    key={col.key}
                    scope="col"
                    className={`px-4 py-2.5 text-xs font-semibold uppercase tracking-wide text-slate-600 ${alignClass(
                      col.align,
                    )}`}
                  >
                    {sortable ? (
                      <button
                        type="button"
                        onClick={() => onSortChange(col.key)}
                        className="inline-flex items-center gap-1 hover:text-slate-900"
                      >
                        {col.header}
                        {isSorted ? (
                          sortDir === 'asc' ? (
                            <ArrowUp size={12} />
                          ) : (
                            <ArrowDown size={12} />
                          )
                        ) : null}
                      </button>
                    ) : (
                      col.header
                    )}
                  </th>
                );
              })}
            </tr>
          </thead>
          <tbody className="divide-y divide-neu-dark/40">
            {rows.length === 0 ? (
              <tr>
                <td colSpan={columns.length}>
                  <EmptyState title={emptyTitle} message={emptyMessage} />
                </td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={rowKey(row)} className="transition-colors hover:bg-black/[0.03]">
                  {columns.map((col) => (
                    <td
                      key={col.key}
                      className={`px-4 py-2.5 text-sm text-slate-700 ${alignClass(col.align)}`}
                    >
                      {col.render ? col.render(row) : row[col.key]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 0 ? (
        <div className="flex items-center justify-between text-sm text-slate-600">
          <span>
            Page {page + 1} of {totalPages} · {totalElements} item
            {totalElements === 1 ? '' : 's'}
          </span>
          <div className="flex gap-1">
            <button
              type="button"
              onClick={() => onPageChange(page - 1)}
              disabled={page <= 0}
              className="btn-xs disabled:opacity-40"
            >
              <ChevronLeft size={16} /> Prev
            </button>
            <button
              type="button"
              onClick={() => onPageChange(page + 1)}
              disabled={page >= totalPages - 1}
              className="btn-xs disabled:opacity-40"
            >
              Next <ChevronRight size={16} />
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}

export default DataTable;
