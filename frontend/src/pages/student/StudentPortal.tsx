import React, { useCallback, useEffect, useState } from 'react';
import { AlertCircle, Bell, BookOpen, Building2, CheckCircle2, Clock3, FileEdit, IdCard, RefreshCw } from 'lucide-react';
import { studentPortalApi, verificationWorkflowApi } from '../../api/client';

type Profile = { id: number; name: string; email?: string; phone?: string; gender: string; maskedApaarId: string; verificationStatus: string; admissionYear: number; category: string; programme: string; institution: string; institutionCode: string };
type Update = { id: number; changes: Record<string, string>; status: string; remarks?: string; createdAt: string };
type VerificationCase = { id: number; status: string; remarks?: string; submittedAt: string; updatedAt: string };

export const StudentPortal: React.FC = () => {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [updates, setUpdates] = useState<Update[]>([]);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [cases, setCases] = useState<VerificationCase[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const [record, history, notices, requestHistory] = await Promise.all([
        studentPortalApi.getProfile(), studentPortalApi.getProfileUpdates(), verificationWorkflowApi.notifications(), verificationWorkflowApi.queue(),
      ]);
      setProfile(record); setUpdates(history); setNotifications(notices); setCases(requestHistory);
      setPhone(record.phone || ''); setEmail(record.email || '');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Your institution has not linked this account to a student record yet. Contact your institution administrator.');
    } finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);

  const requestUpdate = async (event: React.FormEvent) => {
    event.preventDefault(); setBusy(true); setError(''); setMessage('');
    try {
      const changes: Record<string, string> = {};
      if (email !== (profile?.email || '')) changes.email = email;
      if (phone !== (profile?.phone || '')) changes.phone = phone;
      if (!Object.keys(changes).length) { setError('Change your email or phone before submitting.'); return; }
      await studentPortalApi.requestProfileUpdate(changes);
      setMessage('Profile update request sent to your institution for review.'); await load();
    } catch (err: any) { setError(err?.response?.data?.message || 'Could not submit the profile update request.'); }
    finally { setBusy(false); }
  };
  const submitVerification = async () => {
    if (!profile) return;
    setBusy(true); setError(''); setMessage('');
    try { await verificationWorkflowApi.submit(profile.id); setMessage('Verification request submitted. You can track its status below.'); await load(); }
    catch (err: any) { setError(err?.response?.data?.message || 'Could not submit the verification request.'); }
    finally { setBusy(false); }
  };

  if (loading) return <main className="p-6 md:p-10"><div className="animate-pulse h-36 rounded-2xl bg-slate-100" aria-label="Loading student portal" /></main>;
  if (!profile) return <main className="max-w-3xl mx-auto p-6 md:p-10"><section className="rounded-2xl border border-amber-200 bg-amber-50 p-6"><div className="flex gap-3"><AlertCircle className="text-amber-700 shrink-0"/><div><h1 className="font-semibold text-navy text-lg">Student record linking required</h1><p className="text-sm text-slate-700 mt-2">{error}</p><p className="text-xs text-slate-600 mt-3">The institution administrator must link your registered email to the matching academic record. The portal will show your information after that review.</p></div></div><button onClick={() => void load()} className="mt-5 inline-flex gap-2 items-center rounded-lg bg-primary px-4 py-2 text-sm text-white"><RefreshCw size={15}/> Check again</button></section></main>;

  const pending = updates.some((request) => request.status === 'PENDING');
  return <main className="max-w-7xl mx-auto p-5 md:p-8 space-y-6">
    <header className="flex flex-wrap items-start justify-between gap-4"><div><p className="text-xs uppercase tracking-widest text-gold-dark font-bold">Student workspace</p><h1 className="text-2xl md:text-3xl font-semibold text-navy mt-1">Welcome, {profile.name}</h1><p className="text-sm text-slate-600 mt-2">View your academic identity and track institutional verification requests.</p></div><button onClick={() => void load()} className="inline-flex items-center gap-2 rounded-lg border bg-white px-3 py-2 text-sm"><RefreshCw size={15}/> Refresh</button></header>
    {error && <div role="alert" className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}{message && <div role="status" className="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800">{message}</div>}
    <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <Card icon={<IdCard/>} label="APAAR ID" value={profile.maskedApaarId} detail="Masked for privacy"/>
      <Card icon={<CheckCircle2/>} label="Record status" value={profile.verificationStatus.replace(/_/g,' ')} detail="Institutional record status"/>
      <Card icon={<BookOpen/>} label="Programme" value={profile.programme} detail={`Admission year ${profile.admissionYear}`}/>
      <Card icon={<Building2/>} label="Institution" value={profile.institution} detail={profile.institutionCode}/>
    </section>
    <section className="grid gap-6 lg:grid-cols-[1.2fr_.8fr]">
      <article className="rounded-2xl border bg-white p-5 md:p-6"><div className="flex flex-wrap justify-between items-center gap-3"><div className="flex items-center gap-2"><Clock3 className="text-primary" size={18}/><h2 className="font-semibold text-navy">Verification history</h2></div><button onClick={()=>void submitVerification()} disabled={busy||cases.some((c)=>['PENDING','UNDER_REVIEW'].includes(c.status))} className="rounded-lg bg-primary px-3 py-2 text-xs font-semibold text-white disabled:opacity-50">{cases.some((c)=>['PENDING','UNDER_REVIEW'].includes(c.status))?'Request in progress':busy?'Submitting…':'Submit verification request'}</button></div>
        {cases.length ? <ul className="mt-4 divide-y">{cases.map((c)=><li key={c.id} className="py-3"><div className="flex justify-between gap-3"><span className="font-medium text-sm text-navy">Request #{c.id} · {c.status.replace(/_/g,' ')}</span><time className="text-xs text-slate-400">{new Date(c.submittedAt).toLocaleDateString()}</time></div>{c.remarks&&<p className="text-sm text-slate-600 mt-1">{c.remarks}</p>}</li>)}</ul>:<p className="mt-4 text-sm text-slate-500">You have not submitted a verification request.</p>}
        {notifications.length ? <ul className="mt-4 divide-y">{notifications.map((n) => <li key={n.id} className="py-3"><p className="font-medium text-sm text-navy">{n.title}</p><p className="text-sm text-slate-600 mt-1">{n.message}</p><time className="text-xs text-slate-400">{new Date(n.createdAt).toLocaleString()}</time></li>)}</ul> : <div className="mt-4 rounded-xl bg-slate-50 p-5 text-sm text-slate-500 flex gap-2"><Bell size={16}/> No verification updates yet.</div>}
        <h3 className="font-semibold text-navy mt-6">Profile update requests</h3>{updates.length ? <ul className="mt-2 space-y-2">{updates.map((u) => <li key={u.id} className="rounded-lg bg-slate-50 p-3 text-sm"><span className="font-medium">{u.status}</span><span className="text-slate-500"> · {Object.keys(u.changes).join(', ')} · {new Date(u.createdAt).toLocaleDateString()}</span>{u.remarks && <p className="text-slate-600 mt-1">{u.remarks}</p>}</li>)}</ul> : <p className="mt-2 text-sm text-slate-500">No profile updates requested.</p>}
      </article>
      <article className="rounded-2xl border bg-white p-5 md:p-6"><div className="flex items-center gap-2"><FileEdit className="text-primary" size={18}/><h2 className="font-semibold text-navy">Request a profile update</h2></div><p className="text-xs text-slate-500 mt-2">Updates are reviewed by your institution before they change the academic record.</p>
        <form className="mt-4 space-y-4" onSubmit={requestUpdate}><label className="block text-sm font-medium">Email<input type="email" required maxLength={120} value={email} onChange={(e)=>setEmail(e.target.value)} className="mt-1 w-full rounded-lg border p-2.5 font-normal"/></label><label className="block text-sm font-medium">Phone<input type="tel" required maxLength={20} value={phone} onChange={(e)=>setPhone(e.target.value)} className="mt-1 w-full rounded-lg border p-2.5 font-normal"/></label><button disabled={busy||pending} className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-white disabled:opacity-50">{pending?'Request pending':busy?'Submitting…':'Submit for review'}</button></form>
      </article>
    </section>
  </main>;
};

const Card: React.FC<{icon:React.ReactNode;label:string;value:string;detail:string}> = ({icon,label,value,detail}) => <article className="rounded-2xl border bg-white p-4"><div className="flex items-center gap-2 text-primary">{icon}<span className="text-xs uppercase tracking-wide text-slate-500">{label}</span></div><p className="mt-3 font-semibold text-navy truncate">{value}</p><p className="mt-1 text-xs text-slate-500 truncate">{detail}</p></article>;
