import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { institutionApi, uploadApi } from '../../api/client';
import { InstitutionStats, UploadBatch, Programme } from '../../types';
import { StatusChip } from '../../components/StatusChip';
import {
  Users,
  CheckCircle2,
  Clock,
  AlertTriangle,
  AlertCircle,
  TrendingUp,
  Upload,
  FileSpreadsheet,
  ArrowUpRight,
  ShieldCheck,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from 'recharts';

export const InstitutionOverview: React.FC = () => {
  const [stats, setStats] = useState<InstitutionStats | null>(null);
  const [batches, setBatches] = useState<UploadBatch[]>([]);
  const [programmes, setProgrammes] = useState<Programme[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [sData, bData, pData] = await Promise.all([
        institutionApi.getStats(),
        uploadApi.getBatches(undefined, 0, 5),
        institutionApi.getProgrammes(),
      ]);
      setStats(sData);
      setBatches(bData.content);
      setProgrammes(pData);
    } catch (err) {
      console.error('Failed to load institution overview data', err);
    } finally {
      setLoading(false);
    }
  };

  const chartData = [
    { name: 'Valid', count: stats?.validStudents || 0, color: '#2E9E5B' },
    { name: 'Pending', count: stats?.pendingStudents || 0, color: '#F2A100' },
    { name: 'Duplicate', count: stats?.duplicateStudents || 0, color: '#7F5FA8' },
    { name: 'Errors', count: stats?.errorStudents || 0, color: '#D64545' },
  ];

  return (
    <div className="space-y-6">
      {/* Top Banner & Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-primary tracking-wide uppercase">
            Institutional Administration
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Admissions & APAAR Verification Overview
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Real-time validation statuses, quota utilization, and recent batch submissions
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/institution/upload"
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-primary hover:bg-primary-600 text-white font-semibold text-xs shadow-md shadow-blue-500/10 transition-all hover:-translate-y-0.5"
          >
            <Upload className="w-4 h-4" />
            <span>Upload Admissions CSV</span>
          </Link>
          <Link
            to="/institution/students/new"
            className="inline-flex items-center gap-2 px-3.5 py-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs transition-colors"
          >
            <span>+ Single Entry</span>
          </Link>
        </div>
      </div>

      {/* KPI Cards Row */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4">
        {/* Total */}
        <div className="p-4 rounded-2xl bg-white border border-slate-200 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold">Total Students</span>
            <Users className="w-4 h-4 text-slate-400" />
          </div>
          <div className="text-2xl font-extrabold text-navy">
            {stats ? stats.totalStudents.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-slate-400 mt-1">Enrolled records</span>
        </div>

        {/* Valid */}
        <div className="p-4 rounded-2xl bg-emerald-50/50 border border-emerald-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-emerald-800 mb-2">
            <span className="text-xs font-semibold">Valid</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-extrabold text-emerald-900">
            {stats ? stats.validStudents.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-emerald-700 mt-1">Verified with APAAR</span>
        </div>

        {/* Pending */}
        <div className="p-4 rounded-2xl bg-amber-50/50 border border-amber-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-amber-800 mb-2">
            <span className="text-xs font-semibold">Pending</span>
            <Clock className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-extrabold text-amber-900">
            {stats ? stats.pendingStudents.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-amber-700 mt-1">Awaiting review</span>
        </div>

        {/* Duplicates */}
        <div className="p-4 rounded-2xl bg-purple-50/50 border border-purple-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-purple-800 mb-2">
            <span className="text-xs font-semibold">Duplicates</span>
            <AlertTriangle className="w-4 h-4 text-accent-purple" />
          </div>
          <div className="text-2xl font-extrabold text-purple-900">
            {stats ? stats.duplicateStudents.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-purple-700 mt-1">In file / Cross-inst</span>
        </div>

        {/* Errors */}
        <div className="p-4 rounded-2xl bg-red-50/50 border border-red-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-red-800 mb-2">
            <span className="text-xs font-semibold">Errors</span>
            <AlertCircle className="w-4 h-4 text-danger" />
          </div>
          <div className="text-2xl font-extrabold text-red-900">
            {stats ? stats.errorStudents.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-red-700 mt-1">Format / Missing</span>
        </div>

        {/* Intake Utilization */}
        <div className="p-4 rounded-2xl bg-blue-50/50 border border-blue-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-blue-800 mb-2">
            <span className="text-xs font-semibold">Intake Utilization</span>
            <TrendingUp className="w-4 h-4 text-primary" />
          </div>
          <div className="text-2xl font-extrabold text-navy">
            {stats ? `${stats.utilizationPercent}%` : '—'}
          </div>
          <span className="text-[10px] text-primary mt-1">
            Cap: {stats ? stats.totalPermittedIntake : 0}
          </span>
        </div>
      </div>

      {/* Middle Section: Mini Chart & Programme Intake Gauges */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Verification Status Distribution Bar Chart */}
        <div className="lg:col-span-6 bg-white p-5 rounded-2xl border border-slate-200 shadow-soft">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-bold text-slate-800 text-sm">Admissions Status Breakdown</h3>
              <p className="text-[11px] text-slate-400">Distribution across verification rules</p>
            </div>
            <Link to="/institution/issues" className="text-xs font-semibold text-primary hover:underline flex items-center gap-1">
              <span>View Issues</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="h-56 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                <Tooltip
                  cursor={{ fill: 'rgba(0, 112, 192, 0.04)' }}
                  contentStyle={{ borderRadius: '10px', fontSize: '12px', border: '1px solid #e2e8f0' }}
                />
                <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Programme Intake Capacity Meters */}
        <div className="lg:col-span-6 bg-white p-5 rounded-2xl border border-slate-200 shadow-soft">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h3 className="font-bold text-slate-800 text-sm">Intake Capacity by Programme</h3>
              <p className="text-[11px] text-slate-400">AICTE Approved vs Current Enrolments</p>
            </div>
            <span className="text-[10px] font-mono bg-slate-100 text-slate-600 px-2 py-0.5 rounded">
              AY 2026-27
            </span>
          </div>

          <div className="space-y-3.5 mt-4">
            {programmes.slice(0, 5).map((prog) => {
              const totalPermitted = (prog.approvedIntake || 0) + (prog.supernumeraryQuota || 0);
              return (
                <div key={prog.id} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-slate-800">{prog.name}</span>
                    <span className="font-mono text-slate-500 text-[11px]">
                      Quota: {totalPermitted} (Appr: {prog.approvedIntake} + Super: {prog.supernumeraryQuota})
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                    <div
                      className="bg-primary h-2 rounded-full transition-all duration-500"
                      style={{ width: `${Math.min(100, Math.round(((prog.approvedIntake || 1) / totalPermitted) * 100))}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Recent Batch Submissions */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-soft overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
          <div>
            <h3 className="font-bold text-slate-800 text-sm">Recent Upload Batches</h3>
            <p className="text-[11px] text-slate-400">Historical admission file processing logs</p>
          </div>
          <Link
            to="/institution/upload"
            className="text-xs font-semibold text-primary hover:underline flex items-center gap-1"
          >
            <span>Upload New Batch</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {batches.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-xs">
            <FileSpreadsheet className="w-8 h-8 mx-auto text-slate-300 mb-2" />
            No upload batches submitted yet. Click "Upload Admissions CSV" to process your first file.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-[11px] uppercase font-bold text-slate-500 border-b border-slate-100">
                <tr>
                  <th className="py-3 px-6">File Name</th>
                  <th className="py-3 px-4">Uploaded At</th>
                  <th className="py-3 px-4 text-center">Total Rows</th>
                  <th className="py-3 px-4 text-center">Valid</th>
                  <th className="py-3 px-4 text-center">Duplicates</th>
                  <th className="py-3 px-4 text-center">Errors</th>
                  <th className="py-3 px-6 text-right">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {batches.map((b) => (
                  <tr key={b.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3.5 px-6 font-semibold text-slate-900 flex items-center gap-2">
                      <FileSpreadsheet className="w-4 h-4 text-primary flex-shrink-0" />
                      <span className="truncate max-w-xs">{b.filename}</span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-500">
                      {new Date(b.uploadedAt).toLocaleString()}
                    </td>
                    <td className="py-3.5 px-4 text-center font-bold text-slate-800">
                      {b.totalRows}
                    </td>
                    <td className="py-3.5 px-4 text-center font-bold text-emerald-600">
                      {b.validRows}
                    </td>
                    <td className="py-3.5 px-4 text-center font-bold text-accent-purple">
                      {b.duplicateRows}
                    </td>
                    <td className="py-3.5 px-4 text-center font-bold text-danger">
                      {b.errorRows}
                    </td>
                    <td className="py-3.5 px-6 text-right">
                      <StatusChip status={b.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
