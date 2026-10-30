import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Logo } from '../components/Logo';
import { Footer } from '../components/Footer';
import { Shield, Building2, UserCheck, ArrowRight, Lock, Mail, AlertCircle, Sparkles, Play } from 'lucide-react';

export const Login: React.FC = () => {
  const { login, demoAccounts, loading } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    setError(null);
    setIsSubmitting(true);
    try {
      const user = await login(email, password);
      redirectUser(user.role);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid credentials. Try using one-click demo login.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDemoLogin = async (demoEmail: string, demoPass: string) => {
    setError(null);
    setIsSubmitting(true);
    try {
      const user = await login(demoEmail, demoPass);
      redirectUser(user.role);
    } catch (err: any) {
      setError('Demo login failed. Make sure the backend server is running.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const redirectUser = (role: string) => {
    if (role === 'ROLE_AICTE_REGULATOR') {
      navigate('/regulator/dashboard');
    } else if (role === 'ROLE_INSTITUTION_ADMIN') {
      navigate('/institution/overview');
    } else {
      navigate('/verify');
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-gradient-to-br from-slate-50 via-blue-50/30 to-indigo-50/20">
      {/* Top Bar Banner */}
      <header className="border-b border-slate-200/80 bg-white/80 backdrop-blur-md px-6 py-3 shadow-xs">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Logo size="md" />
          <div className="flex items-center gap-3">
            <Link
              to="/demo"
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-gradient-to-r from-amber-500 to-orange-500 text-white font-bold text-xs shadow-sm hover:opacity-90 transition-opacity"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>Watch Video Demo (2:52 min)</span>
            </Link>
            <span className="hidden sm:inline-block text-[11px] font-bold px-2.5 py-1 rounded-full bg-blue-100 text-navy border border-blue-200">
              Smart India Hackathon 2026 &bull; PS 26263
            </span>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-5xl grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          
          {/* Left Column: Mission & Demo Accounts (7 cols) */}
          <div className="lg:col-span-7 space-y-6">
            <div>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100/80 text-primary font-semibold text-xs border border-blue-200 mb-3">
                <Sparkles className="w-3.5 h-3.5" />
                <span>AICTE Regulation Bureau &bull; Theme: Smart Automation</span>
              </div>
              <h1 className="text-3xl sm:text-4xl font-extrabold text-navy tracking-tight leading-tight">
                APAAR ID-Based Student Data Management & Verification
              </h1>
              <p className="mt-2 text-sm text-slate-600 leading-relaxed">
                Centralized institutional registry with automated multi-tier anomaly detection, cross-institutional duplicate identification, live intake compliance tracking, and privacy-preserving QR verification.
              </p>
            </div>

            {/* One-Click Demo Login Cards */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  Select a Demo Persona (One-Click Instant Login)
                </p>
                <span className="text-[11px] text-primary font-medium">Judges Quick Access</span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {demoAccounts.map((account) => {
                  const isReg = account.role === 'ROLE_AICTE_REGULATOR';
                  const isInst = account.role === 'ROLE_INSTITUTION_ADMIN';

                  return (
                    <button
                      key={account.email}
                      type="button"
                      disabled={isSubmitting}
                      onClick={() => handleDemoLogin(account.email, account.password)}
                      className={`text-left p-3.5 rounded-xl border bg-white shadow-xs hover:shadow-md transition-all duration-150 flex flex-col justify-between group hover:-translate-y-0.5 ${
                        isReg
                          ? 'border-purple-200 hover:border-purple-500'
                          : isInst
                          ? 'border-blue-200 hover:border-primary'
                          : 'border-emerald-200 hover:border-emerald-500'
                      }`}
                    >
                      <div>
                        <div className="flex items-center justify-between mb-2">
                          <div
                            className={`p-1.5 rounded-lg ${
                              isReg
                                ? 'bg-purple-100 text-purple-700'
                                : isInst
                                ? 'bg-blue-100 text-primary'
                                : 'bg-emerald-100 text-emerald-700'
                            }`}
                          >
                            {isReg ? (
                              <Shield className="w-4 h-4" />
                            ) : isInst ? (
                              <Building2 className="w-4 h-4" />
                            ) : (
                              <UserCheck className="w-4 h-4" />
                            )}
                          </div>
                          <span className="text-[10px] font-bold text-slate-400 group-hover:text-primary transition-colors flex items-center gap-0.5">
                            Demo <ArrowRight className="w-3 h-3 inline" />
                          </span>
                        </div>
                        <h4 className="font-bold text-xs text-slate-900 group-hover:text-primary transition-colors">
                          {account.title}
                        </h4>
                        <p className="text-[11px] text-slate-500 font-medium truncate mt-0.5">
                          {account.institution}
                        </p>
                      </div>

                      <div className="mt-3 pt-2 border-t border-slate-100 text-[10px] text-slate-400 font-mono">
                        {account.email}
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Key Capabilities Pills */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-2">
              <div className="p-2 rounded-lg bg-white border border-slate-200/80 shadow-2xs">
                <span className="block text-[10px] text-slate-400 font-medium">Validation</span>
                <span className="font-bold text-xs text-slate-800">9 Core Rules</span>
              </div>
              <div className="p-2 rounded-lg bg-white border border-slate-200/80 shadow-2xs">
                <span className="block text-[10px] text-slate-400 font-medium">Intake Engine</span>
                <span className="font-bold text-xs text-slate-800">OK / Warn / Exceed</span>
              </div>
              <div className="p-2 rounded-lg bg-white border border-slate-200/80 shadow-2xs">
                <span className="block text-[10px] text-slate-400 font-medium">Privacy</span>
                <span className="font-bold text-xs text-slate-800">Token & QR Masking</span>
              </div>
              <div className="p-2 rounded-lg bg-white border border-slate-200/80 shadow-2xs">
                <span className="block text-[10px] text-slate-400 font-medium">Compliance</span>
                <span className="font-bold text-xs text-slate-800">Full Audit Trail</span>
              </div>
            </div>
          </div>

          {/* Right Column: Login Card (5 cols) */}
          <div className="lg:col-span-5">
            <div className="bg-white rounded-2xl shadow-xl border border-slate-200/90 p-6 sm:p-8">
              <div className="mb-6">
                <h3 className="text-xl font-bold text-navy">Sign In to APAAR-Connect</h3>
                <p className="text-xs text-slate-500 mt-1">
                  Access institutional data or regulatory bureau dashboard
                </p>
              </div>

              {error && (
                <div className="mb-4 p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Official Email Address
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="admin@bmsce.ac.in"
                      className="w-full text-xs rounded-xl border border-slate-200 pl-9 pr-3 py-2.5 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                    <input
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••••••"
                      className="w-full text-xs rounded-xl border border-slate-200 pl-9 pr-3 py-2.5 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-primary to-navy hover:from-primary-600 hover:to-navy-light text-white font-semibold text-xs shadow-md transition-all duration-150 flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {isSubmitting ? 'Authenticating...' : 'Sign In with Credentials'}
                </button>
              </form>

              <div className="mt-5 pt-4 border-t border-slate-100 text-center">
                <span className="text-[11px] text-slate-500">
                  Default Demo Password: <span className="font-mono font-semibold text-slate-700">Admin@123</span> / <span className="font-mono font-semibold text-slate-700">Regulator@123</span>
                </span>
              </div>
            </div>
          </div>

        </div>
      </main>

      {/* Global Prototype Footer */}
      <Footer />
    </div>
  );
};
