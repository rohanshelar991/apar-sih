import React, { useEffect, useState } from 'react';
import { regulatorApi } from '../../api/client';
import { Bell, ShieldAlert, AlertTriangle, CheckCircle2, Clock, RefreshCw } from 'lucide-react';

export const AlertsFeed: React.FC = () => {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [lastRefreshed, setLastRefreshed] = useState<Date>(new Date());

  useEffect(() => {
    loadAlerts();
    // Real-time polling every 8 seconds
    const interval = setInterval(() => {
      loadAlerts(false);
    }, 8000);
    return () => clearInterval(interval);
  }, []);

  const loadAlerts = async (showLoading = true) => {
    if (showLoading) setLoading(true);
    try {
      const data = await regulatorApi.getAlerts();
      setAlerts(data);
      setLastRefreshed(new Date());
    } catch (err) {
      console.error('Failed to load alerts', err);
    } finally {
      if (showLoading) setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-primary tracking-wide uppercase flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span>Real-Time Alert Feed &bull; Live Telemetry</span>
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Institutional Regulatory Alerts
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Automated notifications of quota exceedances, cross-institution duplicate admissions, and anomalies
          </p>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-[11px] text-slate-400 font-mono">
            Updated: {lastRefreshed.toLocaleTimeString()}
          </span>
          <button
            onClick={() => loadAlerts(true)}
            className="p-2 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-600 transition-colors"
            title="Refresh stream"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Stream List */}
      <div className="space-y-3">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-xs bg-white rounded-2xl border border-slate-200">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
            Connecting to AICTE telemetry stream...
          </div>
        ) : alerts.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-xs bg-white rounded-2xl border border-slate-200">
            <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
            <p className="font-bold text-slate-800 text-sm">All streams normal</p>
            <p className="text-slate-400 mt-1">No critical intake breaches or dual enrolments currently firing.</p>
          </div>
        ) : (
          alerts.map((alert) => (
            <div
              key={alert.id}
              className={`p-4 rounded-xl bg-white border shadow-xs flex items-start gap-3.5 transition-all ${
                alert.level === 'CRITICAL'
                  ? 'border-red-200 hover:border-red-300'
                  : 'border-amber-200 hover:border-amber-300'
              }`}
            >
              <div
                className={`p-2 rounded-lg flex-shrink-0 mt-0.5 ${
                  alert.level === 'CRITICAL' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'
                }`}
              >
                {alert.level === 'CRITICAL' ? (
                  <ShieldAlert className="w-4 h-4" />
                ) : (
                  <AlertTriangle className="w-4 h-4" />
                )}
              </div>

              <div className="flex-1 space-y-1">
                <div className="flex items-center justify-between">
                  <h4 className="font-bold text-xs text-slate-900">{alert.title}</h4>
                  <span className="text-[10px] text-slate-400 font-medium">
                    {new Date(alert.timestamp).toLocaleTimeString()}
                  </span>
                </div>
                <p className="text-xs text-slate-600">{alert.message}</p>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
