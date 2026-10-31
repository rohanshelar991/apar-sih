import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { studentApi, institutionApi } from '../../api/client';
import { Programme } from '../../types';
import {
  UserPlus,
  CheckCircle2,
  AlertCircle,
  ArrowLeft,
  Lock,
  Sparkles,
  ShieldCheck,
} from 'lucide-react';

export const StudentForm: React.FC = () => {
  const navigate = useNavigate();
  const [programmes, setProgrammes] = useState<Programme[]>([]);

  // Form Fields
  const [name, setName] = useState('');
  const [rawApaar, setRawApaar] = useState('');
  const [dob, setDob] = useState('2005-06-15');
  const [gender, setGender] = useState('Male');
  const [programmeId, setProgrammeId] = useState<number | undefined>(undefined);
  const [admissionYear, setAdmissionYear] = useState<number>(2026);
  const [category, setCategory] = useState('REGULAR');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [consentGiven, setConsentGiven] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    institutionApi.getProgrammes().then((progs) => {
      setProgrammes(progs);
      if (progs.length > 0) {
        setProgrammeId(progs[0].id);
      }
    }).catch(console.error);
  }, []);

  // Live APAAR validation computation
  const cleanApaar = rawApaar.replace(/[\s-]+/g, '');
  const isApaar12Digits = /^\d{12}$/.test(cleanApaar);
  const isApaarPartiallyValid = /^\d+$/.test(cleanApaar);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Please provide student full name.');
      return;
    }
    if (!isApaar12Digits) {
      setError('APAAR ID must contain exactly 12 numeric digits.');
      return;
    }
    if (!programmeId) {
      setError('Please select an approved academic programme.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await studentApi.createStudent({
        name: name.trim(),
        apaarId: cleanApaar,
        dob,
        gender,
        programmeId,
        admissionYear,
        category,
        email: email.trim() || undefined,
        phone: phone.trim() || undefined,
        consentGiven,
      });

      navigate('/institution/students');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save student admission record.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Top Banner */}
      <div className="flex items-center justify-between bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/institution/students')}
            className="p-2 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-500 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <span className="text-xs font-bold text-primary tracking-wide uppercase">
              Single Admission Registration
            </span>
            <h2 className="text-2xl font-extrabold text-navy mt-0.5">
              Register Student with APAAR ID
            </h2>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Form Card */}
      <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-slate-200 shadow-soft p-6 sm:p-8 space-y-6">
        {/* APAAR ID with Real-Time Format Feedback */}
        <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
          <div className="flex items-center justify-between">
            <label className="text-xs font-bold text-slate-800 flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-primary" />
              <span>APAAR ID / Automated Permanent Academic Account Registry</span>
            </label>
            <span className="text-[11px] font-mono font-medium text-slate-500">
              {cleanApaar.length} / 12 Digits
            </span>
          </div>

          <div className="relative">
            <input
              type="text"
              required
              maxLength={16}
              value={rawApaar}
              onChange={(e) => setRawApaar(e.target.value)}
              placeholder="e.g. 2026 1122 3344"
              className={`w-full text-sm font-mono tracking-wider rounded-xl border px-3.5 py-2.5 text-slate-800 focus:outline-none transition-colors ${
                isApaar12Digits
                  ? 'border-emerald-400 bg-emerald-50/20 ring-1 ring-emerald-400/30'
                  : cleanApaar.length > 0 && !isApaar12Digits
                  ? 'border-amber-300 bg-amber-50/20'
                  : 'border-slate-300 focus:border-primary'
              }`}
            />
          </div>

          {/* Real-time helper text */}
          <div className="text-[11px] flex items-center gap-1.5">
            {isApaar12Digits ? (
              <span className="text-emerald-700 font-semibold flex items-center gap-1">
                <CheckCircle2 className="w-3.5 h-3.5" />
                Valid 12-digit APAAR format recognized
              </span>
            ) : cleanApaar.length > 0 ? (
              <span className="text-amber-700 flex items-center gap-1">
                <AlertCircle className="w-3.5 h-3.5" />
                Must be exactly 12 numeric digits (hyphens/spaces are normalized)
              </span>
            ) : (
              <span className="text-slate-400">
                Enter the student's 12-digit APAAR ID generated via DigiLocker / ABC portal
              </span>
            )}
          </div>
        </div>

        {/* Basic Details */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Full Legal Name (as in APAAR Card) *
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Aarav Sharma"
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Date of Birth *
            </label>
            <input
              type="date"
              required
              value={dob}
              onChange={(e) => setDob(e.target.value)}
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>
        </div>

        {/* Programme and Year */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="sm:col-span-2">
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Academic Programme & Branch *
            </label>
            <select
              value={programmeId}
              onChange={(e) => setProgrammeId(Number(e.target.value))}
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            >
              {programmes.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.code} - {p.name} (Approved: {p.approvedIntake} + Quota: {p.supernumeraryQuota})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Admission Year *
            </label>
            <select
              value={admissionYear}
              onChange={(e) => setAdmissionYear(Number(e.target.value))}
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            >
              <option value={2026}>2026 - Current Batch</option>
              <option value={2025}>2025</option>
              <option value={2024}>2024</option>
            </select>
          </div>
        </div>

        {/* Gender and Category */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Gender
            </label>
            <select
              value={gender}
              onChange={(e) => setGender(e.target.value)}
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            >
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Other">Other</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Admission Category *
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            >
              <option value="REGULAR">Regular (Sanctioned Merit Intake)</option>
              <option value="SUPERNUMERARY">Supernumerary (TFWS / EWS / Sports)</option>
              <option value="NRI">NRI Quota</option>
              <option value="OCI">OCI (Overseas Citizen of India)</option>
              <option value="FOREIGN_NATIONAL">Foreign National</option>
            </select>
          </div>
        </div>

        {/* Contact Info */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Student Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="student@bmsce.ac.in"
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Student Mobile Phone
            </label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="9876543210"
              className="w-full text-xs rounded-xl border border-slate-200 px-3 py-2.5 text-slate-800 focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>
        </div>

        {/* Consent Checkbox */}
        <div className="p-4 rounded-xl bg-blue-50/70 border border-blue-200/80">
          <label className="flex items-start gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={consentGiven}
              onChange={(e) => setConsentGiven(e.target.checked)}
              className="mt-0.5 rounded text-primary focus:ring-primary w-4 h-4"
            />
            <div className="text-xs text-slate-700">
              <span className="font-bold text-navy block">
                Digital Consent for APAAR Verification & Credential Verification
              </span>
              <p className="text-[11px] text-slate-500 mt-0.5 leading-relaxed">
                The student has provided written or digital authorization permitting the institution and AICTE to verify and store their APAAR / ABC academic identifier in accordance with the Digital Personal Data Protection (DPDP) Act.
              </p>
            </div>
          </label>
        </div>

        {/* Buttons */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
          <button
            type="button"
            onClick={() => navigate('/institution/students')}
            className="px-5 py-2.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
          >
            Cancel
          </button>

          <button
            type="submit"
            disabled={submitting}
            className="px-6 py-2.5 rounded-xl bg-primary hover:bg-primary-600 text-white font-bold text-xs shadow-md transition-colors disabled:opacity-50"
          >
            {submitting ? 'Registering Record...' : 'Save & Run Live Validation'}
          </button>
        </div>
      </form>
    </div>
  );
};
