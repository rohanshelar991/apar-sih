import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { verifyApi } from '../../api/client';
import { PublicVerification } from '../../types';
import { Logo } from '../../components/Logo';
import { Footer } from '../../components/Footer';
import {
  CheckCircle2,
  XCircle,
  ShieldCheck,
  Building2,
  Calendar,
  BookOpen,
  Award,
  Clock,
  ArrowLeft,
  RefreshCw,
  Lock,
} from 'lucide-react';

export const VerifyDetail: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [result, setResult] = useState<PublicVerification | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      loadVerification(token);
    }
  }, [token]);

  const loadVerification = async (t: string) => {
    setLoading(true);
    try {
      const data = await verifyApi.verifyToken(t);
      setResult(data);
    } catch (err: any) {
      setResult({
        valid: false,
        error: err.response?.data?.message || 'Verification service error or token not found.',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-ivory">
      <header className="border-b border-slate-200/80 bg-white/80 backdrop-blur-md px-6 py-3 shadow-xs">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Logo size="md" />
          <Link
            to="/verify"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-primary"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Verify Another</span>
          </Link>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-xl">
          {loading ? (
            <div className="bg-white rounded-2xl p-12 text-center border border-slate-200 shadow-xl space-y-3">
              <RefreshCw className="w-8 h-8 animate-spin mx-auto text-primary" />
              <p className="text-xs font-bold text-slate-700">Validating Cryptographic Token...</p>
              <p className="text-[11px] text-slate-400">Querying AICTE Verification Gateway</p>
            </div>
          ) : result && result.valid ? (
            /* Valid Verified Student Record */
            <div className="bg-white rounded-2xl shadow-xl border border-emerald-200 overflow-hidden">
              {/* Green Verified Header Banner */}
              <div className="bg-gradient-to-r from-emerald-600 to-teal-700 text-white p-6 sm:p-8 text-center space-y-2">
                <div className="w-16 h-16 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center mx-auto shadow-inner">
                  <CheckCircle2 className="w-10 h-10 text-white" />
                </div>
                <h2 className="text-2xl font-extrabold tracking-tight">
                  Authentic Student Credential
                </h2>
                <p className="text-xs text-emerald-100 font-medium">
                  Verified by {result.issuer || 'AICTE APAAR GARUDA NETRA Verification Gateway'}
                </p>
                <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-black/20 text-white text-[11px] font-mono mt-1">
                  <Clock className="w-3 h-3 text-emerald-300" />
                  <span>Verified at: {new Date(result.verifiedAt || '').toLocaleString()}</span>
                </div>
              </div>

              {/* Permitted Disclosed Fields List */}
              <div className="p-6 sm:p-8 space-y-5">
                <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                  <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                    Permitted Disclosed Fields
                  </span>
                  <span className="text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200 flex items-center gap-1">
                    <Lock className="w-3 h-3" />
                    <span>Consent-Enforced Data Minimization</span>
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                  {result.studentName && (
                    <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200/80">
                      <span className="text-[10px] font-bold uppercase text-slate-400 block">
                        Student Full Name
                      </span>
                      <span className="text-sm font-bold text-slate-900 block mt-0.5">
                        {result.studentName}
                      </span>
                    </div>
                  )}

                  {result.maskedApaarId && (
                    <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200/80">
                      <span className="text-[10px] font-bold uppercase text-slate-400 block">
                        APAAR ID (Masked for Privacy)
                      </span>
                      <span className="text-sm font-mono font-bold text-primary block mt-0.5">
                        {result.maskedApaarId}
                      </span>
                    </div>
                  )}

                  {result.institutionName && (
                    <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200/80 sm:col-span-2">
                      <span className="text-[10px] font-bold uppercase text-slate-400 block">
                        Institution / University
                      </span>
                      <span className="text-sm font-semibold text-slate-900 block mt-0.5">
                        {result.institutionName} ({result.institutionCode})
                      </span>
                    </div>
                  )}

                  {result.programmeName && (
                    <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200/80">
                      <span className="text-[10px] font-bold uppercase text-slate-400 block">
                        Academic Programme
                      </span>
                      <span className="text-xs font-semibold text-slate-800 block mt-0.5">
                        {result.programmeName}
                      </span>
                    </div>
                  )}

                  {result.admissionYear && (
                    <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200/80">
                      <span className="text-[10px] font-bold uppercase text-slate-400 block">
                        Admission Batch Year
                      </span>
                      <span className="text-xs font-semibold text-slate-800 block mt-0.5">
                        {result.admissionYear}
                      </span>
                    </div>
                  )}

                  {result.verificationStatus && (
                    <div className="p-3.5 rounded-xl bg-emerald-50/60 border border-emerald-200">
                      <span className="text-[10px] font-bold uppercase text-emerald-800 block">
                        Registry Verification Status
                      </span>
                      <span className="text-xs font-extrabold text-emerald-900 block mt-0.5">
                        {result.verificationStatus} &bull; COMPLIANT
                      </span>
                    </div>
                  )}

                  {result.dob && (
                    <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200/80">
                      <span className="text-[10px] font-bold uppercase text-slate-400 block">
                        Date of Birth
                      </span>
                      <span className="text-xs font-semibold text-slate-800 block mt-0.5">
                        {result.dob}
                      </span>
                    </div>
                  )}
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
                  <span className="font-mono">Token: {token?.substring(0, 14)}...</span>
                  <span>AICTE Central Record Registry</span>
                </div>
              </div>
            </div>
          ) : (
            /* Expired or Invalid Token Error State */
            <div className="bg-white rounded-2xl shadow-xl border border-red-200 overflow-hidden text-center p-8 sm:p-10 space-y-4">
              <div className="w-16 h-16 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto">
                <XCircle className="w-10 h-10" />
              </div>
              <div className="space-y-1">
                <h3 className="text-xl font-extrabold text-slate-900">
                  Credential Verification Failed
                </h3>
                <p className="text-xs text-red-600 font-medium">
                  {result?.error || 'Invalid or expired verification token.'}
                </p>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-500 text-left space-y-1">
                <span className="font-bold text-slate-700 block">Possible Reasons:</span>
                <ul className="list-disc list-inside space-y-0.5 text-[11px]">
                  <li>The token validity period (e.g. 72 hours) has expired.</li>
                  <li>The token string was altered, truncated, or revoked by the institution.</li>
                  <li>Student consent was withdrawn.</li>
                </ul>
              </div>

              <div className="pt-2">
                <Link
                  to="/verify"
                  className="inline-flex items-center gap-1.5 px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-900 text-white font-bold text-xs shadow-md transition-colors"
                >
                  <ArrowLeft className="w-4 h-4" />
                  <span>Try Another Verification Code</span>
                </Link>
              </div>
            </div>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
};
