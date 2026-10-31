import React, { useEffect, useState } from 'react';
import { studentApi, institutionApi, verificationWorkflowApi } from '../../api/client';
import { Student, Programme } from '../../types';
import { StatusChip } from '../../components/StatusChip';
import { QrModal } from '../../components/QrModal';
import {
  Users,
  Search,
  Filter,
  Download,
  QrCode,
  CheckCircle2,
  AlertCircle,
  Eye,
  RefreshCw,
  Calendar,
  Lock,
} from 'lucide-react';

export const StudentsDirectory: React.FC = () => {
  const [students, setStudents] = useState<Student[]>([]);
  const [programmes, setProgrammes] = useState<Programme[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Filters
  const [search, setSearch] = useState('');
  const [programmeId, setProgrammeId] = useState<string>('');
  const [category, setCategory] = useState<string>('');
  const [status, setStatus] = useState<string>('');
  const [year, setYear] = useState<string>('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [requestingId, setRequestingId] = useState<number | null>(null);
  const [workflowMessage, setWorkflowMessage] = useState('');

  // QR Modal
  const [selectedStudentForQr, setSelectedStudentForQr] = useState<Student | null>(null);

  useEffect(() => {
    institutionApi.getProgrammes().then(setProgrammes).catch(console.error);
  }, []);

  useEffect(() => {
    loadStudents();
  }, [programmeId, category, status, year, page]);

  const loadStudents = async () => {
    setLoading(true);
    try {
      const data = await studentApi.getStudents({
        programmeId: programmeId ? Number(programmeId) : undefined,
        category: category || undefined,
        status: status || undefined,
        year: year ? Number(year) : undefined,
        search: search.trim() || undefined,
        page,
        size: 15,
      });
      setStudents(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err) {
      console.error('Failed to load students directory', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    loadStudents();
  };

  const handleExportCsv = async () => {
    setExporting(true);
    try {
      const blob = await studentApi.exportCsv({
        programmeId: programmeId ? Number(programmeId) : undefined,
        category: category || undefined,
        status: status || undefined,
        year: year ? Number(year) : undefined,
      });
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'apaar_students_export.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      console.error('CSV export failed', err);
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
            Institutional Registry
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Student Admissions Directory
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Browse registered students, masked APAAR credentials, verification states, and export official lists
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleExportCsv}
            disabled={exporting}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs shadow-xs transition-colors disabled:opacity-50"
          >
            <Download className="w-4 h-4 text-slate-500" />
            <span>{exporting ? 'Generating CSV...' : 'Export Directory CSV'}</span>
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-soft space-y-3">
        <form onSubmit={handleSearchSubmit} className="flex flex-wrap items-center gap-3">
          <div className="relative flex-1 min-w-[200px]">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search student by name or APAAR ID..."
              className="w-full text-xs rounded-xl border border-slate-200 pl-9 pr-3 py-2 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          <select
            value={programmeId}
            onChange={(e) => {
              setProgrammeId(e.target.value);
              setPage(0);
            }}
            className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none"
          >
            <option value="">All Programmes</option>
            {programmes.map((p) => (
              <option key={p.id} value={p.id}>
                {p.code} - {p.name}
              </option>
            ))}
          </select>

          <select
            value={category}
            onChange={(e) => {
              setCategory(e.target.value);
              setPage(0);
            }}
            className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none"
          >
            <option value="">All Categories</option>
            <option value="REGULAR">Regular</option>
            <option value="SUPERNUMERARY">Supernumerary</option>
            <option value="NRI">NRI</option>
            <option value="OCI">OCI</option>
            <option value="FOREIGN_NATIONAL">Foreign National</option>
          </select>

          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
            className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none"
          >
            <option value="">All Statuses</option>
            <option value="VALID">Valid</option>
            <option value="PENDING">Pending</option>
            <option value="DUPLICATE">Duplicate</option>
            <option value="ERROR">Error</option>
          </select>

          <select
            value={year}
            onChange={(e) => {
              setYear(e.target.value);
              setPage(0);
            }}
            className="text-xs rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-700 font-medium focus:outline-none"
          >
            <option value="">All Years</option>
            <option value="2026">2026</option>
            <option value="2025">2025</option>
            <option value="2024">2024</option>
          </select>

          <button
            type="submit"
            className="px-4 py-2 rounded-xl bg-primary hover:bg-primary-600 text-white font-semibold text-xs transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {workflowMessage && <div role="status" className="rounded-xl border border-blue-200 bg-blue-50 p-3 text-sm text-blue-800">{workflowMessage}</div>}

      {/* Directory Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-soft overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-xs">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
            Loading student records...
          </div>
        ) : students.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-xs">
            <Users className="w-8 h-8 text-slate-300 mx-auto mb-2" />
            No student records found matching the search criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-700">
              <thead className="bg-slate-50 font-bold text-slate-500 text-[11px] uppercase border-b border-slate-100">
                <tr>
                  <th className="py-3 px-6">Student Name</th>
                  <th className="py-3 px-4">APAAR ID (Masked)</th>
                  <th className="py-3 px-4">Programme & Branch</th>
                  <th className="py-3 px-3">Batch</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-6 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {students.map((student) => (
                  <tr key={student.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3.5 px-6">
                      <span className="font-bold text-slate-900 block">{student.name}</span>
                      <span className="text-[11px] text-slate-400">{student.email || 'No email registered'}</span>
                    </td>

                    <td className="py-3.5 px-4 font-mono font-semibold text-primary">
                      {student.maskedApaarId}
                    </td>

                    <td className="py-3.5 px-4">
                      <span className="font-semibold text-slate-800 block">{student.programmeName}</span>
                      <span className="text-[10px] text-slate-400 font-mono">{student.programmeCode}</span>
                    </td>

                    <td className="py-3.5 px-3 font-semibold text-slate-600">
                      {student.admissionYear}
                    </td>

                    <td className="py-3.5 px-4">
                      <span className="text-[11px] font-medium px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">
                        {student.category}
                      </span>
                    </td>

                    <td className="py-3.5 px-4">
                      <StatusChip status={student.verificationStatus} />
                    </td>

                    <td className="py-3.5 px-6 text-right">
                      <div className="flex flex-wrap justify-end gap-2">
                      <button
                        disabled={requestingId === student.id}
                        onClick={async () => { setRequestingId(student.id); setWorkflowMessage(''); try { await verificationWorkflowApi.submit(student.id); setWorkflowMessage(`Review request submitted for ${student.name}.`); } catch (e: any) { setWorkflowMessage(e?.response?.data?.message || 'Could not submit the review request.'); } finally { setRequestingId(null); } }}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-blue-200 bg-blue-50 hover:bg-blue-100 text-blue-800 text-xs font-semibold disabled:opacity-50"
                      >{requestingId === student.id ? 'Submitting…' : 'Request review'}</button>
                      <button
                        onClick={() => setSelectedStudentForQr(student)}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-emerald-200 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 text-xs font-semibold shadow-2xs transition-colors"
                      >
                        <QrCode className="w-3.5 h-3.5 text-emerald-600" />
                        <span>Verified QR Card</span>
                      </button>
                      </div>
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
              Showing page {page + 1} of {totalPages} ({totalElements} total students)
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

      {/* QR Credential Modal */}
      <QrModal
        student={selectedStudentForQr}
        onClose={() => setSelectedStudentForQr(null)}
      />
    </div>
  );
};
