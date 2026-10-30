import React, { useEffect, useState } from 'react';
import { validationApi, institutionApi } from '../../api/client';
import { ValidationIssue, Programme } from '../../types';
import { StatusChip } from '../../components/StatusChip';
import {
  AlertTriangle,
  Filter,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Edit3,
  X,
  Search,
  Check,
  RefreshCw,
  Info,
  Calendar,
  BookOpen
} from 'lucide-react';

export const ValidationIssues: React.FC = () => {
  const [issues, setIssues] = useState<ValidationIssue[]>([]);
  const [programmes, setProgrammes] = useState<Programme[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Filters
  const [statusFilter, setStatusFilter] = useState<string>('OPEN');
  const [severityFilter, setSeverityFilter] = useState<string>('');
  const [ruleCodeFilter, setRuleCodeFilter] = useState<string>('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  // Expanded row tracking
  const [expandedRow, setExpandedRow] = useState<number | null>(null);

  // Correction Modal State
  const [selectedIssue, setSelectedIssue] = useState<ValidationIssue | null>(null);
  const [editName, setEditName] = useState('');
  const [editApaar, setEditApaar] = useState('');
  const [editDob, setEditDob] = useState('');
  const [editProgrammeId, setEditProgrammeId] = useState<number | undefined>(undefined);
  const [editCategory, setEditCategory] = useState('REGULAR');
  const [editYear, setEditYear] = useState<number>(2026);
  const [resolutionNote, setResolutionNote] = useState('');
  const [resolving, setResolving] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    institutionApi.getProgrammes().then(setProgrammes).catch(console.error);
  }, []);

  useEffect(() => {
    loadIssues();
  }, [statusFilter, severityFilter, ruleCodeFilter, page]);

  const loadIssues = async () => {
    setLoading(true);
    try {
      const data = await validationApi.getIssues({
        status: statusFilter || undefined,
        severity: severityFilter || undefined,
        ruleCode: ruleCodeFilter || undefined,
        page,
        size: 15,
      });
      setIssues(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err) {
      console.error('Failed to load issues', err);
    } finally {
      setLoading(false);
    }
  };

  const toggleRow = (id: number) => {
    setExpandedRow(expandedRow === id ? null : id);
  };

  const openCorrectionModal = (issue: ValidationIssue) => {
    setSelectedIssue(issue);
    setEditName(issue.studentName || '');
    setEditApaar(issue.apaarId || '');
    setEditDob('2005-05-15');
    setEditProgrammeId(programmes[0]?.id);
    setEditCategory('REGULAR');
    setEditYear(2026);
    setResolutionNote('Rectified student demographic details & re-validated with APAAR adapter.');
    setSuccessMessage(null);
  };

  const handleResolveSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedIssue) return;

    setResolving(true);
    try {
      await validationApi.resolveIssue(selectedIssue.id, {
        name: editName,
        apaarId: editApaar,
        dob: editDob,
        programmeId: editProgrammeId,
        category: editCategory,
        admissionYear: editYear,
        resolutionNote: resolutionNote,
      });

      setSuccessMessage(`Issue #${selectedIssue.id} resolved successfully! History and audit trail recorded.`);
      setTimeout(() => {
        setSelectedIssue(null);
        setSuccessMessage(null);
        loadIssues();
      }, 1500);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to resolve issue');
    } finally {
      setResolving(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-primary tracking-wide uppercase">
            Validation Center
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Exceptions & Correction Workflow
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Row-level rule violations, cross-institution conflicts, suggested fixes, and inline resolution
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-bold px-3 py-1.5 rounded-xl bg-amber-50 text-amber-900 border border-amber-200">
            {totalElements} Open Exceptions Found
          </span>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-soft flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-1.5 text-xs text-slate-500 font-semibold">
          <Filter className="w-4 h-4 text-slate-400" />
          <span>Filters:</span>
        </div>

        {/* Status Filter */}
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(0);
          }}
          className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-primary/20"
        >
          <option value="">All Statuses</option>
          <option value="OPEN">Open Exceptions</option>
          <option value="RESOLVED">Resolved Issues</option>
        </select>

        {/* Severity Filter */}
        <select
          value={severityFilter}
          onChange={(e) => {
            setSeverityFilter(e.target.value);
            setPage(0);
          }}
          className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-primary/20"
        >
          <option value="">All Severities</option>
          <option value="ERROR">ERROR</option>
          <option value="WARNING">WARNING</option>
          <option value="INFO">INFO</option>
        </select>

        {/* Rule Code Filter */}
        <select
          value={ruleCodeFilter}
          onChange={(e) => {
            setRuleCodeFilter(e.target.value);
            setPage(0);
          }}
          className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-primary/20"
        >
          <option value="">All Rule Codes</option>
          <option value="APAAR_MISSING">APAAR_MISSING</option>
          <option value="APAAR_FORMAT">APAAR_FORMAT</option>
          <option value="DUPLICATE_IN_FILE">DUPLICATE_IN_FILE</option>
          <option value="DUPLICATE_SAME_INSTITUTION">DUPLICATE_SAME_INSTITUTION</option>
          <option value="DUPLICATE_CROSS_INSTITUTION">DUPLICATE_CROSS_INSTITUTION</option>
          <option value="MANDATORY_FIELDS">MANDATORY_FIELDS</option>
          <option value="INCONSISTENT_DATA">INCONSISTENT_DATA</option>
          <option value="INTAKE_EXCEEDED">INTAKE_EXCEEDED</option>
          <option value="APAAR_NOT_FOUND">APAAR_NOT_FOUND</option>
        </select>

        <button
          onClick={() => {
            setStatusFilter('OPEN');
            setSeverityFilter('');
            setRuleCodeFilter('');
            setPage(0);
          }}
          className="text-xs text-primary font-semibold hover:underline ml-auto"
        >
          Reset Filters
        </button>
      </div>

      {/* Issues Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-soft overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-xs">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
            Loading validation issues...
          </div>
        ) : issues.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-xs">
            <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
            <p className="font-bold text-slate-700 text-sm">No validation issues match your criteria</p>
            <p className="text-slate-400 mt-1">All admission records in this filter scope are compliant.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-700">
              <thead className="bg-slate-50 font-bold text-slate-500 text-[11px] uppercase border-b border-slate-100">
                <tr>
                  <th className="py-3 px-4 w-10"></th>
                  <th className="py-3 px-4">Student Name / APAAR</th>
                  <th className="py-3 px-4">Rule Code</th>
                  <th className="py-3 px-3">Severity</th>
                  <th className="py-3 px-6">Message & Suggested Fix</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {issues.map((issue) => {
                  const isExpanded = expandedRow === issue.id;

                  return (
                    <React.Fragment key={issue.id}>
                      <tr className={`hover:bg-slate-50/80 transition-colors ${isExpanded ? 'bg-blue-50/30' : ''}`}>
                        <td className="py-3.5 px-4 text-center">
                          <button
                            onClick={() => toggleRow(issue.id)}
                            className="p-1 rounded hover:bg-slate-200 text-slate-400"
                          >
                            {isExpanded ? (
                              <ChevronUp className="w-4 h-4 text-primary" />
                            ) : (
                              <ChevronDown className="w-4 h-4" />
                            )}
                          </button>
                        </td>

                        <td className="py-3.5 px-4">
                          <span className="font-bold text-slate-900 block">{issue.studentName || 'Row Record'}</span>
                          <span className="font-mono text-[11px] text-slate-400">
                            {issue.maskedApaarId || issue.apaarId || 'N/A'}
                          </span>
                        </td>

                        <td className="py-3.5 px-4">
                          <span className="font-mono font-bold text-[11px] px-2 py-0.5 rounded bg-slate-100 text-slate-700 border border-slate-200">
                            {issue.ruleCode}
                          </span>
                        </td>

                        <td className="py-3.5 px-3">
                          <StatusChip status={issue.severity} showIcon={false} />
                        </td>

                        <td className="py-3.5 px-6 max-w-md">
                          <p className="font-medium text-slate-800 line-clamp-1">{issue.message}</p>
                          {issue.suggestedFix && (
                            <p className="text-[11px] text-slate-500 italic line-clamp-1 mt-0.5">
                              Fix: {issue.suggestedFix}
                            </p>
                          )}
                        </td>

                        <td className="py-3.5 px-4">
                          <StatusChip status={issue.status} />
                        </td>

                        <td className="py-3.5 px-4 text-right">
                          {issue.status === 'OPEN' ? (
                            <button
                              onClick={() => openCorrectionModal(issue)}
                              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-primary hover:bg-primary-600 text-white font-semibold text-xs shadow-xs transition-colors"
                            >
                              <Edit3 className="w-3.5 h-3.5" />
                              <span>Correct</span>
                            </button>
                          ) : (
                            <span className="text-[11px] text-emerald-700 font-medium">
                              Resolved by {issue.resolvedBy?.split('@')[0] || 'Admin'}
                            </span>
                          )}
                        </td>
                      </tr>

                      {/* Expandable Row: Details & Audit Context */}
                      {isExpanded && (
                        <tr className="bg-slate-50/80 border-b border-slate-200">
                          <td colSpan={7} className="p-4 pl-14 space-y-3">
                            <div className="p-3.5 rounded-xl bg-white border border-slate-200 space-y-2 text-xs">
                              <div className="flex items-center gap-2 text-primary font-bold">
                                <Info className="w-4 h-4" />
                                <span>Detailed Regulatory Explanation & Suggested Action</span>
                              </div>
                              <p className="text-slate-800 leading-relaxed">{issue.message}</p>
                              {issue.suggestedFix && (
                                <div className="p-2.5 rounded-lg bg-amber-50/70 border border-amber-200/60 text-amber-900 text-xs">
                                  <span className="font-bold">AICTE Recommendation: </span>
                                  <span>{issue.suggestedFix}</span>
                                </div>
                              )}
                              <div className="flex items-center gap-4 text-[11px] text-slate-400 pt-1">
                                <span>Reported on: {new Date(issue.createdAt).toLocaleString()}</span>
                                {issue.batchId && <span>Upload Batch ID: #{issue.batchId}</span>}
                              </div>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Bar */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-3 border-t border-slate-100 bg-slate-50 text-xs text-slate-600">
            <span>
              Showing page {page + 1} of {totalPages} ({totalElements} total issues)
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

      {/* Inline Correction Modal */}
      {selectedIssue && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs">
          <div className="relative w-full max-w-lg rounded-2xl bg-white shadow-2xl border border-slate-200 overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/50">
              <div className="flex items-center gap-2">
                <div className="p-1.5 rounded-lg bg-blue-100 text-primary">
                  <Edit3 className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-slate-800 text-sm">Correct Flagged Admission Record</h3>
                  <p className="text-[11px] text-slate-500">
                    Fixing issue: <span className="font-mono text-primary font-bold">{selectedIssue.ruleCode}</span>
                  </p>
                </div>
              </div>
              <button
                onClick={() => setSelectedIssue(null)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {successMessage ? (
              <div className="p-8 text-center space-y-3">
                <CheckCircle2 className="w-12 h-12 text-emerald-600 mx-auto" />
                <p className="text-sm font-bold text-slate-800">{successMessage}</p>
                <p className="text-xs text-slate-500">Re-validation succeeded. Status updated.</p>
              </div>
            ) : (
              <form onSubmit={handleResolveSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto">
                <div className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs">
                  <span className="font-bold">Violation: </span>
                  <span>{selectedIssue.message}</span>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Student Full Name
                  </label>
                  <input
                    type="text"
                    required
                    value={editName}
                    onChange={(e) => setEditName(e.target.value)}
                    className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    APAAR ID (12 Numeric Digits)
                  </label>
                  <input
                    type="text"
                    required
                    maxLength={14}
                    value={editApaar}
                    onChange={(e) => setEditApaar(e.target.value)}
                    placeholder="e.g. 202611223344"
                    className="w-full text-xs font-mono rounded-xl border border-slate-200 px-3 py-2 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Date of Birth
                    </label>
                    <input
                      type="date"
                      required
                      value={editDob}
                      onChange={(e) => setEditDob(e.target.value)}
                      className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Category
                    </label>
                    <select
                      value={editCategory}
                      onChange={(e) => setEditCategory(e.target.value)}
                      className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
                    >
                      <option value="REGULAR">Regular</option>
                      <option value="SUPERNUMERARY">Supernumerary</option>
                      <option value="NRI">NRI</option>
                      <option value="OCI">OCI</option>
                      <option value="FOREIGN_NATIONAL">Foreign National</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Programme
                  </label>
                  <select
                    value={editProgrammeId}
                    onChange={(e) => setEditProgrammeId(Number(e.target.value))}
                    className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
                  >
                    {programmes.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.code} - {p.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Correction / Resolution Audit Note
                  </label>
                  <textarea
                    rows={2}
                    required
                    value={resolutionNote}
                    onChange={(e) => setResolutionNote(e.target.value)}
                    placeholder="Explain change for AICTE audit trail..."
                    className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
                  />
                </div>

                <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                  <button
                    type="button"
                    onClick={() => setSelectedIssue(null)}
                    className="px-4 py-2 rounded-xl border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={resolving}
                    className="px-5 py-2 rounded-xl bg-primary hover:bg-primary-600 text-white text-xs font-bold shadow-md disabled:opacity-50 flex items-center gap-1.5"
                  >
                    {resolving ? 'Re-validating...' : 'Re-Validate & Resolve Issue'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
