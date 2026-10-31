import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { Student } from '../types';
import { verifyApi } from '../api/client';
import { ShieldCheck, Copy, Check, ExternalLink, Printer, X, Lock, AlertCircle } from 'lucide-react';

interface QrModalProps {
  student: Student | null;
  onClose: () => void;
}

export const QrModal: React.FC<QrModalProps> = ({ student, onClose }) => {
  const [allowedFields, setAllowedFields] = useState<string[]>([
    'NAME',
    'APAAR',
    'INSTITUTION',
    'PROGRAMME',
    'ADMISSION_YEAR',
    'STATUS',
  ]);
  const [expiryHours, setExpiryHours] = useState<number>(72);
  const [tokenData, setTokenData] = useState<{
    token: string;
    verificationUrl: string;
    expiresAt: string;
  } | null>(null);
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!student) return null;

  const handleToggleField = (field: string) => {
    if (allowedFields.includes(field)) {
      setAllowedFields(allowedFields.filter((f) => f !== field));
    } else {
      setAllowedFields([...allowedFields, field]);
    }
  };

  const handleGenerate = async () => {
    if (!student.consentGiven) {
      setError('Student consent has not been granted. Verification token cannot be generated.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      const resp = await verifyApi.createToken(
        student.id,
        allowedFields.join(','),
        expiryHours
      );
      setTokenData(resp);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate verification token');
    } finally {
      setLoading(false);
    }
  };

  const fullVerifyUrl = tokenData
    ? `${window.location.origin}/verify/${tokenData.token}`
    : '';

  const handleCopyLink = () => {
    if (fullVerifyUrl) {
      navigator.clipboard.writeText(fullVerifyUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs">
      <div className="relative w-full max-w-lg rounded-2xl bg-white shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/50">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-emerald-50 text-emerald-600 border border-emerald-200">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-800 text-sm">Verified Student Credential</h3>
              <p className="text-[11px] text-slate-500">Secure QR Code & Token Generator</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-5 max-h-[80vh] overflow-y-auto">
          {error && (
            <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Student Consent Check */}
          <div className="p-3 rounded-xl bg-blue-50/70 border border-blue-100 text-xs text-slate-700 flex items-start gap-2.5">
            <div className="p-1 rounded bg-blue-100 text-blue-700 mt-0.5">
              <Lock className="w-3.5 h-3.5" />
            </div>
            <div>
              <p className="font-semibold text-blue-950">Student Privacy & Consent</p>
              <p className="text-[11px] text-blue-800/90 mt-0.5">
                {student.consentGiven ? (
                  <span className="text-emerald-700 font-medium">
                    &bull; Student has signed digital consent for academic credential sharing under DPDP Act / AICTE norms.
                  </span>
                ) : (
                  <span className="text-red-600 font-medium">
                    &bull; Consent NOT granted by student. Data sharing is blocked.
                  </span>
                )}
              </p>
            </div>
          </div>

          {!tokenData ? (
            /* Configuration view before generating */
            <div className="space-y-4">
              <div>
                <label className="text-xs font-bold text-slate-700 block mb-1.5">
                  Select Permitted Disclosure Fields (Data Minimization)
                </label>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  {[
                    { key: 'NAME', label: 'Student Name' },
                    { key: 'APAAR', label: 'Masked APAAR ID' },
                    { key: 'INSTITUTION', label: 'Institution Name' },
                    { key: 'PROGRAMME', label: 'Programme & Branch' },
                    { key: 'ADMISSION_YEAR', label: 'Admission Year' },
                    { key: 'STATUS', label: 'Verification Status' },
                    { key: 'DOB', label: 'Date of Birth' },
                    { key: 'GENDER', label: 'Gender' },
                  ].map((field) => (
                    <label
                      key={field.key}
                      className={`flex items-center gap-2 p-2 rounded-lg border text-xs cursor-pointer transition-colors ${
                        allowedFields.includes(field.key)
                          ? 'bg-blue-50/80 border-primary/40 text-blue-900 font-medium'
                          : 'bg-white border-slate-200 text-slate-600'
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={allowedFields.includes(field.key)}
                        onChange={() => handleToggleField(field.key)}
                        className="rounded text-primary focus:ring-primary w-3.5 h-3.5"
                      />
                      <span>{field.label}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-xs font-bold text-slate-700 block mb-1.5">
                  Credential Validity Duration
                </label>
                <select
                  value={expiryHours}
                  onChange={(e) => setExpiryHours(Number(e.target.value))}
                  className="w-full text-xs rounded-lg border border-slate-200 px-3 py-2 bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary/20"
                >
                  <option value={24}>24 Hours (1 Day)</option>
                  <option value={72}>72 Hours (3 Days - Recommended)</option>
                  <option value={168}>168 Hours (7 Days)</option>
                  <option value={720}>720 Hours (30 Days)</option>
                </select>
              </div>

              <button
                onClick={handleGenerate}
                disabled={loading || !student.consentGiven}
                className="w-full py-2.5 px-4 rounded-xl bg-primary hover:bg-primary-600 text-white font-semibold text-xs shadow-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {loading ? 'Generating Secure Token...' : 'Generate Verified QR Credential'}
              </button>
            </div>
          ) : (
            /* Generated QR Credential Card */
            <div className="space-y-4">
              <div
                id="printable-card"
                className="relative rounded-xl border border-slate-200 bg-gradient-to-br from-white to-slate-50 p-5 shadow-sm space-y-4"
              >
                {/* Government Card Banner */}
                <div className="flex items-center justify-between border-b border-slate-200 pb-3">
                  <div className="flex items-center gap-2">
                    <div className="w-6 h-6 rounded bg-navy text-white flex items-center justify-center text-[10px] font-bold">
                      IND
                    </div>
                    <div>
                      <p className="text-[10px] font-extrabold uppercase tracking-wider text-navy">
                        AICTE APAAR GARUDA NETRA
                      </p>
                      <p className="text-[9px] text-slate-500">Student Verified Record</p>
                    </div>
                  </div>
                  <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800">
                    VERIFIED
                  </span>
                </div>

                <div className="flex flex-col sm:flex-row items-center gap-5">
                  <div className="p-2.5 rounded-xl bg-white border border-slate-200 shadow-xs flex-shrink-0">
                    <QRCodeSVG value={fullVerifyUrl} size={130} level="M" />
                  </div>

                  <div className="space-y-1.5 text-xs flex-1 w-full text-slate-700">
                    <div>
                      <span className="text-[10px] font-semibold text-slate-400 block uppercase">Student Name</span>
                      <span className="font-bold text-slate-900">{student.name}</span>
                    </div>

                    <div>
                      <span className="text-[10px] font-semibold text-slate-400 block uppercase">APAAR ID (Masked)</span>
                      <span className="font-mono font-semibold text-primary">{student.maskedApaarId}</span>
                    </div>

                    <div>
                      <span className="text-[10px] font-semibold text-slate-400 block uppercase">Institution</span>
                      <span className="font-medium text-slate-800 truncate block">{student.institutionName}</span>
                    </div>

                    <div className="flex items-center justify-between gap-2 pt-1 border-t border-slate-100 text-[10px] text-slate-500">
                      <span>Prog: {student.programmeCode}</span>
                      <span>Batch: {student.admissionYear}</span>
                    </div>
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-100 text-[10px] text-slate-400 flex items-center justify-between">
                  <span>Expires: {new Date(tokenData.expiresAt).toLocaleDateString()}</span>
                  <span className="font-mono">Token: {tokenData.token.substring(0, 10)}...</span>
                </div>
              </div>

              {/* Direct Link & Actions */}
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <input
                    type="text"
                    readOnly
                    value={fullVerifyUrl}
                    className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 flex-1 text-slate-600 font-mono"
                  />
                  <button
                    onClick={handleCopyLink}
                    className="p-2 rounded-lg border border-slate-200 hover:bg-slate-100 text-slate-600 transition-colors flex items-center gap-1 text-xs"
                  >
                    {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
                    <span>{copied ? 'Copied' : 'Copy'}</span>
                  </button>
                </div>

                <div className="flex items-center gap-2 pt-2">
                  <a
                    href={fullVerifyUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex-1 py-2 px-3 rounded-xl border border-primary text-primary hover:bg-primary-50 text-xs font-semibold text-center transition-colors flex items-center justify-center gap-1.5"
                  >
                    <ExternalLink className="w-3.5 h-3.5" />
                    <span>Open Public Verifier</span>
                  </a>

                  <button
                    onClick={handlePrint}
                    className="py-2 px-3 rounded-xl bg-slate-800 hover:bg-slate-900 text-white text-xs font-semibold transition-colors flex items-center gap-1.5"
                  >
                    <Printer className="w-3.5 h-3.5" />
                    <span>Print Card</span>
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
