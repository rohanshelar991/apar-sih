import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Logo } from '../components/Logo';
import { Footer } from '../components/Footer';
import { GovernmentUtilityBar } from '../components/GovernmentUtilityBar';
import { useLanguage } from '../context/LanguageContext';
import { authApi } from '../api/client';
import { Shield, Building2, UserCheck, ArrowRight, Lock, Mail, AlertCircle, Sparkles } from 'lucide-react';

export const Login: React.FC = () => {
  const { login, demoAccounts, loading } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isRegistering, setIsRegistering] = useState(false);
  const [fullName, setFullName] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    setError(null);
    setIsSubmitting(true);
    try {
      if (isRegistering) {
        if (password.length < 12) { setError('Use a password with at least 12 characters.'); return; }
        const result = await authApi.registerStudent({ email, fullName, password });
        setMessage(result.message); setIsRegistering(false); setPassword(''); return;
      }
      const user = await login(email, password);
      redirectUser(user.role);
    } catch (err: any) {
      setError(err.code === 'ECONNABORTED' || err.code === 'ERR_NETWORK'
        ? 'The backend is waking up or temporarily unavailable. Please try again shortly.'
        : err.response?.data?.message || 'Invalid credentials. Check your email and private demo password.');
    } finally {
      setIsSubmitting(false);
    }
  };
  const [message, setMessage] = useState('');

  const selectDemoPersona = (demoEmail: string) => {
    setEmail(demoEmail);
    setPassword('');
    setError(null);
    document.getElementById('signin-password')?.focus();
  };

  const redirectUser = (role: string) => {
    if (role === 'ROLE_AICTE_REGULATOR' || role === 'ROLE_SUPER_ADMIN') {
      navigate('/regulator/dashboard');
    } else if (role === 'ROLE_INSTITUTION_ADMIN') {
      navigate('/institution/overview');
    } else if (role === 'ROLE_STUDENT') {
      navigate('/student/portal');
    } else if (role === 'ROLE_VERIFICATION_OFFICER' || role === 'ROLE_AUTHORIZED_VERIFIER') {
      navigate('/verification/requests');
    } else {
      navigate('/verify');
    }
  };

  return (
    <div className="garuda-login min-h-screen flex flex-col">
      <GovernmentUtilityBar />
      <header className="border-b border-feather bg-white px-5 sm:px-8 lg:px-12 py-4">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-4">
          <Logo size="md" />
        </div>
      </header>

      <main id="main-content" tabIndex={-1} className="flex-1 flex items-center px-4 sm:px-8 lg:px-12 py-7 lg:py-10">
        <div className="w-full max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-7 lg:gap-10 items-start">
          <div className="lg:col-span-7 space-y-6">
            <section className="garuda-hero relative overflow-hidden rounded-2xl px-6 py-7 sm:p-9 text-white">
              <div className="flex items-center gap-2 text-[10px] font-semibold uppercase tracking-[0.16em] text-[#D8BA7D] mb-5">
                <span className="w-6 h-px bg-gold" />
                {t('loginKicker')}
              </div>
              <h1 className="relative max-w-xl text-[32px] sm:text-[40px] xl:text-[44px] leading-[1.15] font-semibold tracking-tight">
                {t('loginHeadline')}
              </h1>
              <p className="relative mt-5 text-sm leading-relaxed text-[#D0D8E5] max-w-lg">
                {t('loginDescription')}
              </p>
              <div className="mt-7 flex items-center gap-2 text-[11px] tracking-wide text-[#D8BA7D]">
                <Shield className="w-3.5 h-3.5" />
                A Vision for Academic Trust.
              </div>
            </section>

            <section aria-labelledby="demo-personas-heading">
              <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
                <h2 id="demo-personas-heading" className="text-[11px] font-bold uppercase tracking-[0.12em] text-navy">{t('explorePersona')}</h2>
                <span className="text-[10px] text-slate-500">One-click instant login · Judges Quick Access</span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {demoAccounts.map((account) => {
                  const isReg = account.role === 'ROLE_AICTE_REGULATOR';
                  const isInst = account.role === 'ROLE_INSTITUTION_ADMIN';
                  return (
                    <button key={account.email} type="button" disabled={isSubmitting}
                      onClick={() => selectDemoPersona(account.email)}
                      className="garuda-persona group rounded-xl p-4 text-left bg-white flex flex-col justify-between transition-colors disabled:opacity-50">
                      <div>
                        <div className="flex items-center justify-between mb-3">
                          <span className={`p-2 rounded-lg ${isReg ? 'bg-gold-light text-gold-dark' : isInst ? 'bg-primary-50 text-primary' : 'bg-success-light text-success'}`}>
                            {isReg ? <Shield className="w-4 h-4" /> : isInst ? <Building2 className="w-4 h-4" /> : <UserCheck className="w-4 h-4" />}
                          </span>
                          <ArrowRight className="w-3.5 h-3.5 text-slate-400 group-hover:text-primary" />
                        </div>
                        <h3 className="font-semibold text-xs leading-relaxed text-navy">{account.title}</h3>
                        <p className="text-[10px] text-slate-500 leading-relaxed mt-1">{account.institution}</p>
                      </div>
                      <div className="mt-3 pt-3 border-t border-feather text-[9px] text-slate-500 break-all">{account.email}</div>
                    </button>
                  );
                })}
              </div>
            </section>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 border-t border-feather pt-4">
              <div><span className="block text-[10px] text-slate-500 mb-1">Validation</span><span className="font-semibold text-[11px] text-navy">9 Core Rules</span></div>
              <div><span className="block text-[10px] text-slate-500 mb-1">Intake Engine</span><span className="font-semibold text-[11px] text-navy">OK / Warn / Exceed</span></div>
              <div><span className="block text-[10px] text-slate-500 mb-1">Privacy</span><span className="font-semibold text-[11px] text-navy">Token &amp; QR Masking</span></div>
              <div><span className="block text-[10px] text-slate-500 mb-1">Compliance</span><span className="font-semibold text-[11px] text-navy">Full Audit Trail</span></div>
            </div>
          </div>

          <div className="lg:col-span-5 lg:pt-5">
            <div className="garuda-signin bg-white rounded-2xl border border-feather p-6 sm:p-8 xl:p-10">
              <div className="flex items-center gap-2 text-[10px] font-bold tracking-[0.18em] uppercase text-gold-dark mb-6">
                <span className="w-5 h-px bg-gold" /> Welcome back
              </div>
              <div className="mb-7">
                <h2 className="text-[24px] leading-tight font-semibold text-navy">{isRegistering ? t('registering') : <>Sign in to<br />APAAR GARUDA NETRA</>}</h2>
                <p className="text-xs leading-relaxed text-slate-500 mt-3">{isRegistering ? 'An institution administrator must link your account to your academic record.' : t('accessWorkspace')}</p>
              </div>
              {error && (
                <div role="alert" className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" /><span>{error}</span>
                </div>
              )}
              {message && <div role="status" className="mb-4 p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs">{message}</div>}
              <form onSubmit={handleSubmit} className="space-y-5">
                {isRegistering && <div><label htmlFor="signup-name" className="block text-xs font-semibold text-navy mb-2">Full name</label><input id="signup-name" required maxLength={120} value={fullName} onChange={(e)=>setFullName(e.target.value)} className="w-full text-sm rounded-lg border border-feather px-3 py-3" autoComplete="name"/></div>}
                <div>
                  <label htmlFor="signin-email" className="block text-xs font-semibold text-navy mb-2">{t('email')}</label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5" />
                    <input id="signin-email" type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)}
                      placeholder="admin@example.com" className="w-full text-sm rounded-lg border border-feather bg-white pl-10 pr-3 py-3 text-navy placeholder:text-slate-400 focus:ring-2 focus:ring-primary/15 focus:border-primary" />
                  </div>
                </div>
                <div>
                  <label htmlFor="signin-password" className="block text-xs font-semibold text-navy mb-2">{t('password')}</label>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5" />
                    <input id="signin-password" type="password" autoComplete={isRegistering ? 'new-password' : 'current-password'} minLength={isRegistering?12:undefined} value={password} onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••••••" className="w-full text-sm rounded-lg border border-feather bg-white pl-10 pr-3 py-3 text-navy placeholder:text-slate-400 focus:ring-2 focus:ring-primary/15 focus:border-primary" />
                  </div>
                </div>
                <button type="submit" disabled={isSubmitting}
                  className="w-full py-3.5 px-4 rounded-lg bg-primary hover:bg-primary-600 text-white font-semibold text-xs transition-colors flex items-center justify-center gap-2 disabled:opacity-50">
                  {isSubmitting ? 'Please wait…' : isRegistering ? t('registering') : t('signIn')}<ArrowRight className="w-4 h-4" />
                </button>
              </form>
              <button type="button" onClick={()=>{setIsRegistering((v)=>!v);setError(null);setMessage('');}} className="mt-4 w-full text-xs font-semibold text-primary hover:underline">{isRegistering?t('signIn'):t('register')}</button>
              <div className="mt-6 pt-5 border-t border-feather">
                <div className="flex items-center gap-2 text-[10px] text-slate-500"><Lock className="w-3 h-3" />Institutional access · Role-based workspace</div>
                <p className="mt-4 text-[10px] leading-relaxed text-slate-500">Isolated demo accounts only. Enter the private password supplied locally. These demo identities imply no institutional or employer affiliation.</p>
              </div>
            </div>
            <p className="text-center text-[10px] text-slate-500 mt-5">APAAR GARUDA NETRA · A Vision for Academic Trust.</p>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};
