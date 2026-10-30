import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  HelpCircle,
  Play,
  ShieldCheck,
  CheckCircle2,
  FileSpreadsheet,
  AlertTriangle,
  QrCode,
  FileText,
  Info,
  Layers,
  ArrowRight
} from 'lucide-react';

export const HelpGuide: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleStartDemo = async () => {
    // 1-Click launcher for judges: log into BMSCE Institution Admin and navigate to Upload
    try {
      await login('admin@bmsce.ac.in', 'Admin@123');
      navigate('/institution/upload');
    } catch (e) {
      console.error(e);
      navigate('/login');
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-primary tracking-wide uppercase">
            Smart India Hackathon 2026 &bull; Problem Statement 26263
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Platform Architecture & Judges Demo Walkthrough
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            "Development of an APAAR ID-Based Student Data Management and Verification Platform for AICTE-Approved Institutions"
          </p>
        </div>

        <button
          onClick={handleStartDemo}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-primary to-navy hover:from-primary-600 hover:to-navy-light text-white font-bold text-xs shadow-md transition-all hover:-translate-y-0.5"
        >
          <Play className="w-4 h-4 fill-current" />
          <span>Launch 3-Min Live Demo</span>
        </button>
      </div>

      {/* Honesty Rule & Simulation Adapter Badge */}
      <div className="p-4 rounded-2xl bg-amber-50/80 border border-amber-200/80 text-xs text-amber-950 space-y-2">
        <div className="flex items-center gap-2 font-bold text-amber-900">
          <Info className="w-4 h-4 text-amber-600" />
          <span>Architectural Honesty Note: Simulated APAAR Verification Adapter</span>
        </div>
        <p className="text-[11px] leading-relaxed text-amber-900/90">
          In this prototype, all student records and APAAR identities are <strong>synthetic</strong>. There is no live UIDAI / Academic Bank of Credits (ABC) server integration. The system implements an enterprise-grade <code className="bg-amber-100 px-1.5 py-0.5 rounded font-mono font-semibold">ApaarVerificationAdapter</code> interface so production government DigiLocker/ABC APIs can be dropped in seamlessly without modifying core business logic.
        </p>
      </div>

      {/* 5-Step Demo Script Walkthrough */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-soft p-6 sm:p-8 space-y-6">
        <h3 className="text-lg font-bold text-navy flex items-center gap-2">
          <Layers className="w-5 h-5 text-primary" />
          <span>Live Hackathon Evaluation Script (Step-by-Step)</span>
        </h3>

        <div className="space-y-4">
          {/* Step 1 */}
          <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 flex items-start gap-3.5">
            <span className="w-6 h-6 rounded-full bg-primary text-white font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
              1
            </span>
            <div className="space-y-1 text-xs">
              <h4 className="font-bold text-slate-900">Institution Login & Batch Upload</h4>
              <p className="text-slate-600 leading-relaxed">
                Log in as <strong>BMS College of Engineering Admin</strong> (<code className="font-mono text-primary">admin@bmsce.ac.in</code>). Navigate to <strong>Upload Admissions</strong>, click <em>"Load SIH Demo CSV (~60 rows)"</em>, preview the parsed rows with PapaParse, and click <em>"Submit Batch for Server Validation"</em>.
              </p>
            </div>
          </div>

          {/* Step 2 */}
          <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 flex items-start gap-3.5">
            <span className="w-6 h-6 rounded-full bg-primary text-white font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
              2
            </span>
            <div className="space-y-1 text-xs">
              <h4 className="font-bold text-slate-900">Rule Violation & Duplicate Detection</h4>
              <p className="text-slate-600 leading-relaxed">
                Observe the 9-Rule validation engine detect:
              </p>
              <ul className="list-disc list-inside space-y-0.5 text-slate-600 pl-1 text-[11px]">
                <li><strong className="text-purple-700">Cross-Institution Duplicate:</strong> APAAR <code className="font-mono">202488339101</code> already enrolled at COEP Pune (INST002).</li>
                <li><strong className="text-purple-700">In-File Duplicate:</strong> APAAR <code className="font-mono">202699110022</code> appearing twice in the same batch.</li>
                <li><strong className="text-red-600">Format & Missing Field Errors:</strong> Non-numeric APAAR, invalid length, missing DOB.</li>
                <li><strong className="text-amber-600">Intake Exceeded:</strong> BMSCE CSE enrolments pushed past 138 total permitted quota!</li>
              </ul>
            </div>
          </div>

          {/* Step 3 */}
          <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 flex items-start gap-3.5">
            <span className="w-6 h-6 rounded-full bg-primary text-white font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
              3
            </span>
            <div className="space-y-1 text-xs">
              <h4 className="font-bold text-slate-900">Inline Correction Workflow & Audit Log</h4>
              <p className="text-slate-600 leading-relaxed">
                Go to <strong>Validation Issues</strong>, click <em>"Correct"</em> on a flagged record, fix the APAAR / Date of Birth, add an audit note, and submit. The system re-validates in real time, resolves the exception, and writes immutable entries to both <code className="font-mono">student_history</code> and <code className="font-mono">audit_logs</code>.
              </p>
            </div>
          </div>

          {/* Step 4 */}
          <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 flex items-start gap-3.5">
            <span className="w-6 h-6 rounded-full bg-primary text-white font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
              4
            </span>
            <div className="space-y-1 text-xs">
              <h4 className="font-bold text-slate-900">AICTE Regulator Dashboard & Anomaly Center</h4>
              <p className="text-slate-600 leading-relaxed">
                Switch to <strong>AICTE Regulator</strong> (<code className="font-mono text-primary">regulator@aicte-india.org</code>). View national KPIs, the grouped bar chart, and notice BMSCE CSE status turned to <strong>EXCEEDED (&gt;100%)</strong>. Open the <strong>Anomaly Center</strong> to review the cross-institution duplicate and perform an <em>Escalate</em> or <em>Review</em> action.
              </p>
            </div>
          </div>

          {/* Step 5 */}
          <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 flex items-start gap-3.5">
            <span className="w-6 h-6 rounded-full bg-primary text-white font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
              5
            </span>
            <div className="space-y-1 text-xs">
              <h4 className="font-bold text-slate-900">Summary Report & Public QR Verification</h4>
              <p className="text-slate-600 leading-relaxed">
                Generate and download official PDF & CSV summary reports. Finally, generate a Verified QR Code for an authorized student and inspect the public verification page (<code className="font-mono text-primary">/verify/:token</code>) demonstrating consent enforcement and strict data minimization.
              </p>
            </div>
          </div>
        </div>

        {/* Action Trigger */}
        <div className="pt-4 border-t border-slate-100 flex items-center justify-between">
          <span className="text-xs text-slate-500 font-medium">Ready to see the platform in action?</span>
          <button
            onClick={handleStartDemo}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-primary hover:bg-primary-600 text-white font-bold text-xs shadow-sm transition-colors"
          >
            <span>Start Live Demo Scenario</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
