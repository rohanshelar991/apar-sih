import React from 'react';
import { ShieldCheck, Info } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="mt-auto border-t border-slate-200 bg-white/80 backdrop-blur-sm py-3 px-6 text-xs text-slate-500 no-print">
      <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          {/* Visible but unobtrusive prototype badge */}
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-medium bg-amber-50 text-amber-900 border border-amber-200/80 shadow-sm">
            <Info className="w-3.5 h-3.5 text-amber-600 flex-shrink-0" />
            <span>Prototype – simulated APAAR verification</span>
          </span>
          <span className="hidden md:inline text-slate-300">|</span>
          <span className="hidden md:inline text-slate-500 text-[11px]">
            Smart India Hackathon 2026 (PS 26263) &bull; Team AuthenIQ
          </span>
        </div>

        <div className="flex items-center gap-4 text-[11px] text-slate-500">
          <span>AICTE Regulation Bureau &bull; Theme: Smart Automation</span>
          <span className="text-slate-300">&bull;</span>
          <span className="text-slate-400">Synthetic Data Only</span>
        </div>
      </div>
    </footer>
  );
};
