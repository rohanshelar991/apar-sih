import React from 'react';
import { Info } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Footer: React.FC = () => {
  return (
    <footer className="mt-auto border-t border-slate-200 bg-white/80 backdrop-blur-sm py-3 px-6 text-xs text-slate-500 no-print">
      <div className="max-w-7xl mx-auto flex flex-col xl:flex-row items-start xl:items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          {/* Visible but unobtrusive prototype badge */}
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-medium bg-amber-50 text-amber-900 border border-amber-200/80 shadow-sm">
            <Info className="w-3.5 h-3.5 text-amber-600 flex-shrink-0" />
            <span>Demonstration prototype · verification is simulated</span>
          </span>
          <span className="hidden md:inline text-slate-300">|</span>
          <span className="hidden md:inline text-slate-500 text-[11px]">
            Smart India Hackathon 2026 (PS 26263) &bull; Team AuthenIQ
          </span>
        </div>

        <div className="text-[10px] text-slate-500 space-y-1">
          <div className="flex flex-wrap gap-x-3 gap-y-1"><span className="font-semibold text-navy">APAAR GARUDA NETRA</span><span>A Vision for Academic Trust.</span></div>
          <div>Not an official government service &bull; No official APAAR API integration &bull; Synthetic data only</div>
          <nav aria-label="Footer links" className="flex gap-4 pt-1">
            <Link className="font-semibold text-primary hover:underline" to="/verify">Public verification</Link>
            <Link className="font-semibold text-primary hover:underline" to="/help">Help and guidance</Link>
          </nav>
        </div>
      </div>
    </footer>
  );
};
