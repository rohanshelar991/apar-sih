import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { QrCode, Search, ShieldCheck, ArrowRight, Lock, Sparkles, AlertCircle } from 'lucide-react';
import { Logo } from '../../components/Logo';
import { Footer } from '../../components/Footer';

export const VerifierPage: React.FC = () => {
  const [tokenInput, setTokenInput] = useState('');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleVerifySubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const cleanToken = tokenInput.trim();
    if (!cleanToken) {
      setError('Please enter a verification code or token.');
      return;
    }
    // If user pasted full URL, extract token
    if (cleanToken.includes('/verify/')) {
      const parts = cleanToken.split('/verify/');
      navigate(`/verify/${parts[1]}`);
    } else {
      navigate(`/verify/${cleanToken}`);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-ivory">
      <header className="border-b border-slate-200/80 bg-white/80 backdrop-blur-md px-6 py-3 shadow-xs">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Logo size="md" />
          <span className="text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-100 text-emerald-800 border border-emerald-200">
            Public Gateway &bull; Privacy-Preserving Verification
          </span>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-xl bg-white rounded-2xl shadow-xl border border-slate-200/90 p-6 sm:p-8 space-y-6">
          <div className="text-center space-y-2">
            <div className="w-14 h-14 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-600 flex items-center justify-center mx-auto shadow-sm">
              <QrCode className="w-8 h-8" />
            </div>
            <h2 className="text-2xl font-extrabold text-navy">
              Verify Student Academic Credential
            </h2>
            <p className="text-xs text-slate-500 max-w-md mx-auto">
              Scan student QR code or enter the secure 32-character verification token provided on official transcripts or digital student cards.
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleVerifySubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Enter Credential Verification Token / URL
              </label>
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="text"
                  value={tokenInput}
                  onChange={(e) => setTokenInput(e.target.value)}
                  placeholder="e.g. 5d89f81a1729... or full verification URL"
                  className="w-full text-xs font-mono rounded-xl border border-slate-200 pl-9 pr-3 py-3 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-700 hover:from-emerald-700 hover:to-teal-800 text-white font-bold text-xs shadow-md shadow-emerald-600/10 transition-all flex items-center justify-center gap-2"
            >
              <ShieldCheck className="w-4 h-4" />
              <span>Verify Official Credential</span>
            </button>
          </form>

          {/* Privacy Guarantee Box */}
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-600 space-y-1.5">
            <div className="flex items-center gap-1.5 font-bold text-slate-900">
              <Lock className="w-3.5 h-3.5 text-primary" />
              <span>Data Minimization & Privacy Guarantee</span>
            </div>
            <p className="text-[11px] leading-relaxed text-slate-500">
              In accordance with AICTE data security and DPDP Act provisions, this portal displays <strong>only the specific fields permitted by the student</strong> during consent generation. Sensitive demographic details remain cryptographically masked.
            </p>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};
