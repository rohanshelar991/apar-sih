import React, { useEffect, useState } from 'react';
import { regulatorApi } from '../../api/client';
import { Anomaly } from '../../types';
import { StatusChip } from '../../components/StatusChip';
import {
  ShieldAlert,
  AlertTriangle,
  Users,
  CheckCircle2,
  XCircle,
  TrendingUp,
  Mail,
  X,
  FileCheck,
  Send,
  RefreshCw,
} from 'lucide-react';

export const AnomalyCenter: React.FC = () => {
  const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
  const [loading, setLoading] = useState(true);

  // Action Modal State
  const [selectedAnomaly, setSelectedAnomaly] = useState<Anomaly | null>(null);
  const [actionType, setActionType] = useState<'REVIEW' | 'DISMISS' | 'ESCALATE'>('REVIEW');
  const [actionNote, setActionNote] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  useEffect(() => {
    loadAnomalies();
  }, []);

  const loadAnomalies = async () => {
    setLoading(true);
    try {
      const data = await regulatorApi.getAnomalies();
      setAnomalies(data);
    } catch (err) {
      console.error('Failed to load anomalies', err);
    } finally {
      setLoading(false);
    }
  };

  const openActionModal = (anomaly: Anomaly, defaultAction: 'REVIEW' | 'DISMISS' | 'ESCALATE') => {
    setSelectedAnomaly(anomaly);
    setActionType(defaultAction);
    setActionNote(
      defaultAction === 'ESCALATE'
        ? 'Escalated to AICTE Regional Officer & Institution Registrar for immediate formal explanation.'
        : defaultAction === 'DISMISS'
        ? 'Verified with institution registrar as approved student transfer.'
        : 'Initiated formal review under AICTE Approval Process Handbook (APH) guidelines.'
    );
    setSuccessMsg(null);
  };

  const handleActionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAnomaly) return;

    setSubmitting(true);
    try {
      await regulatorApi.performAnomalyAction(selectedAnomaly.id, actionType, actionNote);
      setSuccessMsg(`Action '${actionType}' recorded successfully! Regulatory audit log updated.`);
      setTimeout(() => {
        setSelectedAnomaly(null);
        setSuccessMsg(null);
        loadAnomalies();
      }, 1500);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit regulator action');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-red-600 tracking-wide uppercase flex items-center gap-1.5">
            <ShieldAlert className="w-4 h-4 text-red-600" />
            <span>AICTE Regulatory Intelligence</span>
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Anomaly & Duplicate Center
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Detect suspicious cross-institutional dual enrolments, intake breaches, and clustered admission patterns
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-bold px-3 py-1.5 rounded-xl bg-red-50 text-red-700 border border-red-200">
            {anomalies.length} Flagged Anomalies
          </span>
        </div>
      </div>

      {/* Anomalies List */}
      <div className="space-y-4">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-xs bg-white rounded-2xl border border-slate-200">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
            Scanning national student registry for anomalies...
          </div>
        ) : anomalies.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-xs bg-white rounded-2xl border border-slate-200">
            <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
            <p className="font-bold text-slate-800 text-sm">No unresolved anomalies found</p>
            <p className="text-slate-400 mt-1">All cross-institutional records are clean and compliant.</p>
          </div>
        ) : (
          anomalies.map((anomaly) => {
            const isCrossInst = anomaly.type === 'DUPLICATE_CROSS_INSTITUTION';
            const isExceeded = anomaly.type === 'INTAKE_CAPACITY_EXCEEDED';
            const isCluster = anomaly.type === 'SHARED_CONTACT_CLUSTER';

            return (
              <div
                key={anomaly.id}
                className={`p-5 rounded-2xl bg-white border transition-all duration-200 shadow-soft hover:shadow-md ${
                  isCrossInst
                    ? 'border-red-200/90 hover:border-red-400'
                    : isExceeded
                    ? 'border-amber-200/90 hover:border-amber-400'
                    : 'border-purple-200/90 hover:border-purple-400'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                  <div className="flex items-start gap-3.5">
                    <div
                      className={`p-2.5 rounded-xl flex-shrink-0 ${
                        isCrossInst
                          ? 'bg-red-100 text-red-700'
                          : isExceeded
                          ? 'bg-amber-100 text-amber-700'
                          : 'bg-purple-100 text-purple-700'
                      }`}
                    >
                      {isCrossInst ? (
                        <Users className="w-5 h-5" />
                      ) : isExceeded ? (
                        <TrendingUp className="w-5 h-5" />
                      ) : (
                        <Mail className="w-5 h-5" />
                      )}
                    </div>

                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <h4 className="text-sm font-bold text-slate-900">{anomaly.title}</h4>
                        <span
                          className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                            anomaly.severity === 'HIGH'
                              ? 'bg-red-100 text-red-700'
                              : 'bg-amber-100 text-amber-800'
                          }`}
                        >
                          {anomaly.severity} PRIORITY
                        </span>
                      </div>

                      <p className="text-xs text-slate-600 leading-relaxed max-w-3xl">
                        {anomaly.description}
                      </p>

                      <div className="flex flex-wrap items-center gap-4 text-[11px] text-slate-400 pt-1 font-medium">
                        <span>Institution: <span className="font-semibold text-slate-700">{anomaly.institutionName} ({anomaly.institutionCode})</span></span>
                        {anomaly.maskedApaarId && (
                          <span>Student APAAR: <span className="font-mono font-semibold text-primary">{anomaly.maskedApaarId}</span></span>
                        )}
                        <span>Detected: {new Date(anomaly.createdAt).toLocaleString()}</span>
                      </div>
                    </div>
                  </div>

                  {/* Actions: Review, Dismiss, Escalate */}
                  <div className="flex items-center gap-2 self-end sm:self-center flex-shrink-0">
                    <button
                      onClick={() => openActionModal(anomaly, 'REVIEW')}
                      className="px-3 py-1.5 rounded-xl border border-blue-200 bg-blue-50 hover:bg-blue-100 text-blue-900 font-semibold text-xs transition-colors"
                    >
                      Review
                    </button>

                    <button
                      onClick={() => openActionModal(anomaly, 'DISMISS')}
                      className="px-3 py-1.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 font-semibold text-xs transition-colors"
                    >
                      Dismiss
                    </button>

                    <button
                      onClick={() => openActionModal(anomaly, 'ESCALATE')}
                      className="px-3 py-1.5 rounded-xl bg-red-600 hover:bg-red-700 text-white font-semibold text-xs shadow-xs transition-colors"
                    >
                      Escalate
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Action Modal */}
      {selectedAnomaly && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs">
          <div className="relative w-full max-w-lg rounded-2xl bg-white shadow-2xl border border-slate-200 overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/50">
              <div className="flex items-center gap-2">
                <ShieldAlert className="w-5 h-5 text-red-600" />
                <div>
                  <h3 className="font-bold text-slate-800 text-sm">Regulatory Intervention Action</h3>
                  <p className="text-[11px] text-slate-500">Anomaly ID #{selectedAnomaly.id}</p>
                </div>
              </div>
              <button
                onClick={() => setSelectedAnomaly(null)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {successMsg ? (
              <div className="p-8 text-center space-y-3">
                <CheckCircle2 className="w-12 h-12 text-emerald-600 mx-auto" />
                <p className="text-sm font-bold text-slate-800">{successMsg}</p>
              </div>
            ) : (
              <form onSubmit={handleActionSubmit} className="p-6 space-y-4">
                <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-1">
                  <span className="font-bold text-slate-900 block">{selectedAnomaly.title}</span>
                  <p className="text-slate-600 text-[11px]">{selectedAnomaly.description}</p>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Select Regulatory Action Type
                  </label>
                  <div className="grid grid-cols-3 gap-2 text-xs">
                    {(['REVIEW', 'DISMISS', 'ESCALATE'] as const).map((type) => (
                      <button
                        key={type}
                        type="button"
                        onClick={() => {
                          setActionType(type);
                          if (type === 'ESCALATE') {
                            setActionNote('Formal notice issued under AICTE approval norms regarding dual enrolment breach.');
                          } else if (type === 'DISMISS') {
                            setActionNote('Dismissed: verified authorized student migration certificate.');
                          } else {
                            setActionNote('Placed under investigation pending university response.');
                          }
                        }}
                        className={`py-2 px-3 rounded-xl border font-bold text-xs transition-colors ${
                          actionType === type
                            ? type === 'ESCALATE'
                              ? 'bg-red-600 border-red-600 text-white shadow-sm'
                              : type === 'REVIEW'
                              ? 'bg-primary border-primary text-white shadow-sm'
                              : 'bg-slate-800 border-slate-800 text-white shadow-sm'
                            : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                        }`}
                      >
                        {type}
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Official Regulatory Note / Action Log *
                  </label>
                  <textarea
                    rows={3}
                    required
                    value={actionNote}
                    onChange={(e) => setActionNote(e.target.value)}
                    placeholder="Provide specific regulatory context for immutable audit record..."
                    className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
                  />
                </div>

                <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                  <button
                    type="button"
                    onClick={() => setSelectedAnomaly(null)}
                    className="px-4 py-2 rounded-xl border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="px-5 py-2 rounded-xl bg-primary hover:bg-primary-600 text-white text-xs font-bold shadow-md disabled:opacity-50 flex items-center gap-1.5"
                  >
                    {submitting ? 'Recording...' : 'Commit Regulatory Decision'}
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
