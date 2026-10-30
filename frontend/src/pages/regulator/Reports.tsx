import React, { useState, useEffect } from 'react';
import { reportApi, institutionApi } from '../../api/client';
import { Institution } from '../../types';
import { FileText, Download, FileSpreadsheet, Building2, Calendar, ShieldCheck, Check } from 'lucide-react';

export const Reports: React.FC = () => {
  const [institutions, setInstitutions] = useState<Institution[]>([]);
  const [selectedInstId, setSelectedInstId] = useState<string>('');
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [downloadingCsv, setDownloadingCsv] = useState(false);
  const [pdfSuccess, setPdfSuccess] = useState(false);
  const [csvSuccess, setCsvSuccess] = useState(false);

  useEffect(() => {
    institutionApi.getAll().then(setInstitutions).catch(console.error);
  }, []);

  const handleDownloadPdf = async () => {
    setDownloadingPdf(true);
    setPdfSuccess(false);
    try {
      const blob = await reportApi.downloadPdf(selectedInstId ? Number(selectedInstId) : undefined);
      const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'aicte_regulatory_summary_report.pdf');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setPdfSuccess(true);
      setTimeout(() => setPdfSuccess(false), 3000);
    } catch (err) {
      console.error('PDF download failed', err);
      alert('Failed to generate PDF report.');
    } finally {
      setDownloadingPdf(false);
    }
  };

  const handleDownloadCsv = async () => {
    setDownloadingCsv(true);
    setCsvSuccess(false);
    try {
      const blob = await reportApi.downloadCsv(selectedInstId ? Number(selectedInstId) : undefined);
      const url = window.URL.createObjectURL(new Blob([blob], { type: 'text/csv' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'aicte_regulatory_summary_report.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setCsvSuccess(true);
      setTimeout(() => setCsvSuccess(false), 3000);
    } catch (err) {
      console.error('CSV download failed', err);
      alert('Failed to generate CSV report.');
    } finally {
      setDownloadingCsv(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-primary tracking-wide uppercase">
            Official Regulatory Documentation
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            AICTE Regulatory Summary Reports
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Generate and export signed compliance reports containing intake analyses, exception logs, and correction audits
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-bold px-3 py-1.5 rounded-xl bg-purple-50 text-purple-900 border border-purple-200">
            Authorized Regulatory Review Only
          </span>
        </div>
      </div>

      {/* Scope Selector */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-soft space-y-4">
        <h3 className="text-sm font-bold text-slate-800">Report Scope & Generation Parameters</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Select Institution Filter (Leave blank for All Institutions)
            </label>
            <select
              value={selectedInstId}
              onChange={(e) => setSelectedInstId(e.target.value)}
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-primary/20"
            >
              <option value="">All 10 AICTE Accredited Institutions (National Overview)</option>
              {institutions.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.code} - {i.name} ({i.state})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Academic Session
            </label>
            <div className="flex items-center gap-2 px-3 py-2.5 rounded-xl border border-slate-200 bg-slate-50 text-xs font-medium text-slate-600">
              <Calendar className="w-4 h-4 text-slate-400" />
              <span>AY 2026-2027 &bull; Active AICTE Intake Cycle</span>
            </div>
          </div>
        </div>

        {/* Action Download Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-slate-100">
          {/* PDF Card */}
          <div className="p-5 rounded-2xl border border-blue-200 bg-gradient-to-br from-blue-50/50 to-white flex flex-col justify-between space-y-4">
            <div className="flex items-start gap-3">
              <div className="p-2.5 rounded-xl bg-primary text-white shadow-xs">
                <FileText className="w-6 h-6" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-navy">Official Regulatory Summary (PDF)</h4>
                <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                  Includes AICTE letterhead, approved vs actual intake comparison table, active exception breakdown, and demographic compliance summary.
                </p>
              </div>
            </div>

            <button
              onClick={handleDownloadPdf}
              disabled={downloadingPdf}
              className="w-full py-2.5 px-4 rounded-xl bg-primary hover:bg-primary-600 text-white font-bold text-xs shadow-md transition-all flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {downloadingPdf ? (
                'Generating PDF Document...'
              ) : pdfSuccess ? (
                <>
                  <Check className="w-4 h-4 text-emerald-300" />
                  <span>PDF Downloaded Successfully!</span>
                </>
              ) : (
                <>
                  <Download className="w-4 h-4" />
                  <span>Download Summary PDF</span>
                </>
              )}
            </button>
          </div>

          {/* CSV Card */}
          <div className="p-5 rounded-2xl border border-emerald-200 bg-gradient-to-br from-emerald-50/50 to-white flex flex-col justify-between space-y-4">
            <div className="flex items-start gap-3">
              <div className="p-2.5 rounded-xl bg-emerald-600 text-white shadow-xs">
                <FileSpreadsheet className="w-6 h-6" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-emerald-950">Raw Audit & Intake Data (CSV)</h4>
                <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                  Machine-readable spreadsheet containing programme-by-programme intake utilization, open exception rules, and resolution audit notes.
                </p>
              </div>
            </div>

            <button
              onClick={handleDownloadCsv}
              disabled={downloadingCsv}
              className="w-full py-2.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-md transition-all flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {downloadingCsv ? (
                'Generating CSV...'
              ) : csvSuccess ? (
                <>
                  <Check className="w-4 h-4 text-emerald-200" />
                  <span>CSV Downloaded Successfully!</span>
                </>
              ) : (
                <>
                  <Download className="w-4 h-4" />
                  <span>Download Summary CSV</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
