import React, { useEffect, useState } from 'react';
import { auditApi } from '../../api/client';
import { AuditLog } from '../../types';
import { History, Filter, Download, Search, RefreshCw, Shield, Clock } from 'lucide-react';

export const AuditLogs: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Filters
  const [actor, setActor] = useState('');
  const [action, setAction] = useState('');
  const [role, setRole] = useState('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    loadLogs();
  }, [action, role, page]);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const data = await auditApi.getLogs({
        actor: actor.trim() || undefined,
        action: action || undefined,
        role: role || undefined,
        page,
        size: 20,
      });
      setLogs(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err) {
      console.error('Failed to load audit logs', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    loadLogs();
  };

  const handleExportCsv = async () => {
    setExporting(true);
    try {
      const blob = await auditApi.exportCsv({ actor, action, role });
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'aicte_audit_trail_export.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      console.error('Audit CSV export failed', err);
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-primary tracking-wide uppercase">
            Compliance & Regulatory Traceability
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Immutable Audit Trail Viewer
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Complete audit trail recording logins, batch uploads, corrections, verification checks, and reports
          </p>
        </div>

        <button
          onClick={handleExportCsv}
          disabled={exporting}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs shadow-xs transition-colors disabled:opacity-50"
        >
          <Download className="w-4 h-4 text-slate-500" />
          <span>{exporting ? 'Exporting...' : 'Export Audit CSV'}</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-soft">
        <form onSubmit={handleSearchSubmit} className="flex flex-wrap items-center gap-3">
          <div className="relative flex-1 min-w-[200px]">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={actor}
              onChange={(e) => setActor(e.target.value)}
              placeholder="Filter by Actor Email / User..."
              className="w-full text-xs rounded-xl border border-slate-200 pl-9 pr-3 py-2 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          <select
            value={action}
            onChange={(e) => {
              setAction(e.target.value);
              setPage(0);
            }}
            className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none"
          >
            <option value="">All Actions</option>
            <option value="LOGIN_SUCCESS">LOGIN_SUCCESS</option>
            <option value="UPLOAD_BATCH">UPLOAD_BATCH</option>
            <option value="UPDATE_STUDENT">UPDATE_STUDENT</option>
            <option value="RESOLVE_ISSUE">RESOLVE_ISSUE</option>
            <option value="GENERATE_VERIFICATION_TOKEN">GENERATE_VERIFICATION_TOKEN</option>
            <option value="VERIFY_RECORD">VERIFY_RECORD</option>
            <option value="GENERATE_REPORT_PDF">GENERATE_REPORT_PDF</option>
            <option value="GENERATE_REPORT_CSV">GENERATE_REPORT_CSV</option>
          </select>

          <select
            value={role}
            onChange={(e) => {
              setRole(e.target.value);
              setPage(0);
            }}
            className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none"
          >
            <option value="">All Roles</option>
            <option value="ROLE_INSTITUTION_ADMIN">INSTITUTION_ADMIN</option>
            <option value="ROLE_AICTE_REGULATOR">AICTE_REGULATOR</option>
            <option value="ROLE_AUTHORIZED_VERIFIER">AUTHORIZED_VERIFIER</option>
            <option value="ROLE_SYSTEM">SYSTEM</option>
          </select>

          <button
            type="submit"
            className="px-4 py-2 rounded-xl bg-primary hover:bg-primary-600 text-white font-semibold text-xs transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {/* Logs Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-soft overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-xs">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
            Loading audit records...
          </div>
        ) : logs.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-xs">
            <History className="w-8 h-8 text-slate-300 mx-auto mb-2" />
            No audit logs found matching the filter criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-700">
              <thead className="bg-slate-50 font-bold text-slate-500 text-[10px] uppercase border-b border-slate-100">
                <tr>
                  <th className="py-3 px-4">Timestamp</th>
                  <th className="py-3 px-4">Actor</th>
                  <th className="py-3 px-3">Role</th>
                  <th className="py-3 px-3">Action</th>
                  <th className="py-3 px-3">Entity</th>
                  <th className="py-3 px-3">IP Address</th>
                  <th className="py-3 px-6">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono text-[11px]">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 text-slate-500 whitespace-nowrap">
                      {new Date(log.timestamp).toLocaleString()}
                    </td>

                    <td className="py-3 px-4 font-semibold text-slate-900">
                      {log.actor}
                    </td>

                    <td className="py-3 px-3">
                      <span className="text-[10px] font-sans px-2 py-0.5 rounded-full bg-slate-100 text-slate-700 font-medium">
                        {log.role.replace('ROLE_', '')}
                      </span>
                    </td>

                    <td className="py-3 px-3 font-bold text-primary">
                      {log.action}
                    </td>

                    <td className="py-3 px-3 text-slate-600">
                      {log.entity} #{log.entityId}
                    </td>

                    <td className="py-3 px-3 text-slate-400">
                      {log.ip || '127.0.0.1'}
                    </td>

                    <td className="py-3 px-6 font-sans text-xs text-slate-700 max-w-sm truncate">
                      {log.details}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-3 border-t border-slate-100 bg-slate-50 text-xs text-slate-600">
            <span>
              Showing page {page + 1} of {totalPages} ({totalElements} audit entries)
            </span>
            <div className="flex items-center gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="px-3 py-1 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 disabled:opacity-40"
              >
                Previous
              </button>
              <button
                disabled={page + 1 >= totalPages}
                onClick={() => setPage(page + 1)}
                className="px-3 py-1 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 disabled:opacity-40"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
