import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { regulatorApi, institutionApi } from '../../api/client';
import {
  RegulatorKpi,
  ProgrammeIntake,
  CategoryBreakdown,
  YearTrend,
  Institution,
} from '../../types';
import { StatusChip } from '../../components/StatusChip';
import {
  Building2,
  Users,
  ShieldAlert,
  TrendingUp,
  AlertTriangle,
  Award,
  Download,
  Filter,
  CheckCircle2,
  ArrowUpRight,
  RefreshCw,
  Bell,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from 'recharts';

export const RegulatorDashboard: React.FC = () => {
  const [kpi, setKpi] = useState<RegulatorKpi | null>(null);
  const [institutions, setInstitutions] = useState<Institution[]>([]);
  const [selectedInstId, setSelectedInstId] = useState<string>('');
  const [programmeIntakes, setProgrammeIntakes] = useState<ProgrammeIntake[]>([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState<CategoryBreakdown[]>([]);
  const [yearTrends, setYearTrends] = useState<YearTrend[]>([]);
  const [loading, setLoading] = useState(true);

  // Category chart colors
  const CATEGORY_COLORS = ['#244B83', '#23865B', '#D99A32', '#718096', '#E65100'];

  useEffect(() => {
    institutionApi.getAll().then(setInstitutions).catch(console.error);
    loadDashboardData();
  }, []);

  useEffect(() => {
    loadProgrammeData(selectedInstId ? Number(selectedInstId) : undefined);
  }, [selectedInstId]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [kData, cData, tData] = await Promise.all([
        regulatorApi.getKpis(),
        regulatorApi.getCategoryBreakdown(),
        regulatorApi.getYearTrends(),
      ]);
      setKpi(kData);
      setCategoryBreakdown(cData);
      setYearTrends(tData);
    } catch (err) {
      console.error('Failed to load regulator dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  const loadProgrammeData = async (instId?: number) => {
    try {
      const pData = await regulatorApi.getProgrammeIntakes(instId);
      setProgrammeIntakes(pData);
    } catch (err) {
      console.error('Failed to load programme intake data', err);
    }
  };

  // Grouped bar chart preparation: aggregate by Programme Code (CSE, ECE, ME, CE, MBA)
  const groupedChartData = ['CSE', 'ECE', 'ME', 'CE', 'MBA'].map((code) => {
    const matching = programmeIntakes.filter((p) => p.programmeCode === code);
    const approved = matching.reduce((sum, p) => sum + p.approvedIntake, 0);
    const actual = matching.reduce((sum, p) => sum + p.actualEnrolment, 0);
    const supernumerary = matching.reduce((sum, p) => sum + p.supernumeraryQuota, 0);

    return {
      programme: code,
      Approved: approved,
      Supernumerary: supernumerary,
      ActualEnrolment: actual,
    };
  });

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-purple-700 tracking-wide uppercase">
            AICTE Regulation Bureau &bull; Smart Automation Oversight
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            National Intake & Verification Dashboard
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Real-time compliance monitoring across AICTE-approved institutions via APAAR ID linkage
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/regulator/reports"
            className="inline-flex items-center gap-1.5 px-3.5 py-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs shadow-xs transition-colors"
          >
            <Download className="w-4 h-4 text-slate-500" />
            <span>Generate Reports (PDF/CSV)</span>
          </Link>

          <Link
            to="/regulator/anomalies"
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-red-600 hover:bg-red-700 text-white font-semibold text-xs shadow-md shadow-red-500/10 transition-colors"
          >
            <ShieldAlert className="w-4 h-4" />
            <span>Anomaly Center</span>
          </Link>
        </div>
      </div>

      {/* Header KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4">
        {/* Institutions */}
        <div className="p-4 rounded-2xl bg-white border border-slate-200 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold">Institutions</span>
            <Building2 className="w-4 h-4 text-purple-600" />
          </div>
          <div className="text-2xl font-extrabold text-navy">
            {kpi ? kpi.totalInstitutions : '—'}
          </div>
          <span className="text-[10px] text-slate-400 mt-1">Accredited entities</span>
        </div>

        {/* Approved Intake */}
        <div className="p-4 rounded-2xl bg-blue-50/50 border border-blue-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-blue-800 mb-2">
            <span className="text-xs font-semibold">Approved Intake</span>
            <Award className="w-4 h-4 text-primary" />
          </div>
          <div className="text-2xl font-extrabold text-navy">
            {kpi ? kpi.approvedIntake.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-primary mt-1">Sanctioned seats</span>
        </div>

        {/* Supernumerary Seats */}
        <div className="p-4 rounded-2xl bg-indigo-50/50 border border-indigo-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-indigo-800 mb-2">
            <span className="text-xs font-semibold">Supernumerary</span>
            <Users className="w-4 h-4 text-indigo-600" />
          </div>
          <div className="text-2xl font-extrabold text-indigo-950">
            {kpi ? kpi.supernumerarySeats.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-indigo-700 mt-1">TFWS, EWS, NRI quotas</span>
        </div>

        {/* Actual Enrolment */}
        <div className="p-4 rounded-2xl bg-emerald-50/50 border border-emerald-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-emerald-800 mb-2">
            <span className="text-xs font-semibold">Actual Enrolment</span>
            <TrendingUp className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-extrabold text-emerald-950">
            {kpi ? kpi.actualEnrolment.toLocaleString() : '—'}
          </div>
          <span className="text-[10px] text-emerald-700 mt-1">Verified with APAAR</span>
        </div>

        {/* Open Exceptions */}
        <div className="p-4 rounded-2xl bg-amber-50/50 border border-amber-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-amber-800 mb-2">
            <span className="text-xs font-semibold">Open Exceptions</span>
            <AlertTriangle className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-extrabold text-amber-950">
            {kpi ? kpi.openExceptions : '—'}
          </div>
          <span className="text-[10px] text-amber-700 mt-1">Duplicates & anomalies</span>
        </div>

        {/* Intake Exceeded Alerts */}
        <div className="p-4 rounded-2xl bg-red-50/50 border border-red-200/80 shadow-soft flex flex-col justify-between">
          <div className="flex items-center justify-between text-red-800 mb-2">
            <span className="text-xs font-semibold">Exceeded Quota</span>
            <ShieldAlert className="w-4 h-4 text-red-600" />
          </div>
          <div className="text-2xl font-extrabold text-red-700">
            {kpi ? kpi.intakeExceededProgrammes : '0'}
          </div>
          <span className="text-[10px] text-red-600 mt-1">Programmes over cap</span>
        </div>
      </div>

      {/* Main Charts Row: Grouped Bar Chart with Institution Selector */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Approved vs Actual Enrolment Grouped Bar Chart (8 cols) */}
        <div className="lg:col-span-8 bg-white p-6 rounded-2xl border border-slate-200 shadow-soft space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
            <div>
              <h3 className="text-sm font-bold text-slate-800">
                Approved Intake vs Actual Enrolment by Programme
              </h3>
              <p className="text-[11px] text-slate-400">
                Comparing Sanctioned Seats, Supernumerary Quota, and Registered APAAR Enrolments
              </p>
            </div>

            {/* Institution Selector Dropdown */}
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-slate-500">Institution:</span>
              <select
                value={selectedInstId}
                onChange={(e) => setSelectedInstId(e.target.value)}
                className="text-xs rounded-xl border border-slate-200 px-3 py-1.5 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-primary/20 max-w-[220px]"
              >
                <option value="">All 10 Institutions (Aggregate)</option>
                {institutions.map((inst) => (
                  <option key={inst.id} value={inst.id}>
                    {inst.code} - {inst.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="h-72 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={groupedChartData} margin={{ top: 20, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="programme" tick={{ fontSize: 12, fill: '#475569' }} />
                <YAxis tick={{ fontSize: 11, fill: '#475569' }} />
                <Tooltip
                  cursor={{ fill: 'rgba(31, 73, 125, 0.04)' }}
                  contentStyle={{ borderRadius: '12px', fontSize: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}
                />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                <Bar dataKey="Approved" fill="#101D35" radius={[4, 4, 0, 0]} name="Approved Intake" />
                <Bar dataKey="Supernumerary" fill="#718096" radius={[4, 4, 0, 0]} name="Supernumerary Quota" />
                <Bar dataKey="ActualEnrolment" fill="#244B83" radius={[4, 4, 0, 0]} name="Actual Enrolment (APAAR)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Category Breakdown Donut Chart (4 cols) */}
        <div className="lg:col-span-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-soft flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-800">
              Admission Category Breakdown
            </h3>
            <p className="text-[11px] text-slate-400 mt-0.5">
              Regular, Supernumerary, NRI, OCI distribution
            </p>

            <div className="h-52 w-full mt-2">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryBreakdown}
                    dataKey="count"
                    nameKey="category"
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={80}
                    paddingAngle={3}
                  >
                    {categoryBreakdown.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={CATEGORY_COLORS[index % CATEGORY_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ borderRadius: '10px', fontSize: '12px', border: '1px solid #e2e8f0' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="space-y-1.5 pt-3 border-t border-slate-100 text-xs">
            {categoryBreakdown.map((cat, idx) => (
              <div key={cat.category} className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span
                    className="w-2.5 h-2.5 rounded-full flex-shrink-0"
                    style={{ backgroundColor: CATEGORY_COLORS[idx % CATEGORY_COLORS.length] }}
                  />
                  <span className="text-slate-600 font-medium">{cat.category}</span>
                </div>
                <span className="font-bold text-slate-800">
                  {cat.count} ({cat.percentage}%)
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Multi-Year Trend & Intake Status Table */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Trend Line (4 cols) */}
        <div className="lg:col-span-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-soft flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-800">
              Multi-Year Enrolment Trend
            </h3>
            <p className="text-[11px] text-slate-400 mt-0.5">
              Annual admission trajectory across reporting years
            </p>

            <div className="h-52 w-full mt-4">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={yearTrends} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="year" tick={{ fontSize: 11, fill: '#64748b' }} />
                  <YAxis tick={{ fontSize: 11, fill: '#64748b' }} />
                  <Tooltip
                    contentStyle={{ borderRadius: '10px', fontSize: '12px', border: '1px solid #e2e8f0' }}
                  />
                  <Line
                    type="monotone"
                    dataKey="count"
                    stroke="#244B83"
                    strokeWidth={3}
                    dot={{ r: 4, fill: '#101D35' }}
                    name="Total Enrolments"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-blue-50/50 border border-blue-100 text-[11px] text-blue-900 mt-2">
            <span className="font-bold">&bull; Consistent growth: </span>
            <span>Steady 12% annual increase in APAAR ID linkage compliance since 2024.</span>
          </div>
        </div>

        {/* Programme Intake Utilization Status Table (8 cols) */}
        <div className="lg:col-span-8 bg-white rounded-2xl border border-slate-200 shadow-soft overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
            <div>
              <h3 className="text-sm font-bold text-slate-800">
                Intake Capacity Compliance Table
              </h3>
              <p className="text-[11px] text-slate-400">
                Status by programme: OK (&lt;90%), Warning (90-100%), Exceeded (&gt;100%)
              </p>
            </div>
            <span className="text-[11px] font-mono bg-slate-100 text-slate-600 px-2 py-0.5 rounded">
              {programmeIntakes.length} Programmes Active
            </span>
          </div>

          <div className="overflow-x-auto max-h-80 overflow-y-auto">
            <table className="w-full text-left text-xs text-slate-700">
              <thead className="bg-slate-50 font-bold text-slate-500 text-[10px] uppercase border-b border-slate-100 sticky top-0">
                <tr>
                  <th className="py-2.5 px-4">Inst Code</th>
                  <th className="py-2.5 px-4">Programme</th>
                  <th className="py-2.5 px-3 text-center">Approved</th>
                  <th className="py-2.5 px-3 text-center">Quota</th>
                  <th className="py-2.5 px-3 text-center">Enrolled</th>
                  <th className="py-2.5 px-4 text-center">Utilization</th>
                  <th className="py-2.5 px-4 text-right">Compliance Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {programmeIntakes.map((prog) => (
                  <tr key={prog.programmeId} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-2.5 px-4 font-mono font-bold text-slate-900">
                      {prog.institutionCode}
                    </td>

                    <td className="py-2.5 px-4">
                      <span className="font-semibold text-slate-800 block truncate max-w-[180px]">
                        {prog.programmeName}
                      </span>
                    </td>

                    <td className="py-2.5 px-3 text-center font-mono text-slate-600">
                      {prog.approvedIntake}
                    </td>

                    <td className="py-2.5 px-3 text-center font-mono text-slate-500">
                      +{prog.supernumeraryQuota}
                    </td>

                    <td className="py-2.5 px-3 text-center font-bold text-slate-900">
                      {prog.actualEnrolment}
                    </td>

                    <td className="py-2.5 px-4 text-center font-bold">
                      <span
                        className={
                          prog.utilizationPercent > 100
                            ? 'text-red-600'
                            : prog.utilizationPercent >= 90
                            ? 'text-amber-600'
                            : 'text-emerald-600'
                        }
                      >
                        {prog.utilizationPercent}%
                      </span>
                    </td>

                    <td className="py-2.5 px-4 text-right">
                      <StatusChip status={prog.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};
