import React from 'react';
import { CheckCircle2, Clock, AlertTriangle, AlertCircle, ShieldAlert } from 'lucide-react';

interface StatusChipProps {
  status: string;
  className?: string;
  showIcon?: boolean;
}

export const StatusChip: React.FC<StatusChipProps> = ({ status, className = '', showIcon = true }) => {
  const norm = (status || '').toUpperCase().trim();

  let bg = 'bg-slate-100 text-slate-700 border-slate-200';
  let icon = <Clock className="w-3.5 h-3.5" />;
  let label = status;

  if (norm === 'VALID' || norm === 'VERIFIED' || norm === 'RESOLVED' || norm === 'OK' || norm === 'COMPLETED') {
    bg = 'bg-emerald-50 text-emerald-800 border-emerald-200/80';
    icon = <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />;
    label = norm === 'OK' ? 'Intake OK' : norm === 'VALID' ? 'Valid' : norm;
  } else if (norm === 'PENDING' || norm === 'WARNING' || norm === 'UNDER_REVIEW') {
    bg = 'bg-amber-50 text-amber-800 border-amber-200/80';
    icon = <Clock className="w-3.5 h-3.5 text-amber-600" />;
    label = norm === 'WARNING' ? 'Near Capacity (90-100%)' : norm === 'PENDING' ? 'Pending Review' : norm;
  } else if (norm === 'DUPLICATE') {
    bg = 'bg-rose-50 text-rose-800 border-rose-200/80';
    icon = <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />;
    label = 'Duplicate';
  } else if (norm === 'ERROR' || norm === 'EXCEEDED' || norm === 'ESCALATED') {
    bg = 'bg-red-50 text-red-700 border-red-300 ring-1 ring-red-400/20'; // Red outline
    icon = <AlertCircle className="w-3.5 h-3.5 text-red-600" />;
    label = norm === 'EXCEEDED' ? 'Intake Exceeded (>100%)' : 'Validation Error';
  } else if (norm === 'DISMISSED') {
    bg = 'bg-slate-100 text-slate-600 border-slate-300';
    icon = <CheckCircle2 className="w-3.5 h-3.5 text-slate-400" />;
    label = 'Dismissed';
  }

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold border transition-all ${bg} ${className}`}
    >
      {showIcon && icon}
      <span>{label}</span>
    </span>
  );
};
