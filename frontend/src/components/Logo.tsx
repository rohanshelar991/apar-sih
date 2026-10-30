import React from 'react';

interface LogoProps {
  className?: string;
  size?: 'sm' | 'md' | 'lg';
  showSubtitle?: boolean;
}

export const Logo: React.FC<LogoProps> = ({ className = '', size = 'md', showSubtitle = true }) => {
  const iconSize = size === 'sm' ? 'w-7 h-7' : size === 'lg' ? 'w-12 h-12' : 'w-9 h-9';
  const titleSize = size === 'sm' ? 'text-lg' : size === 'lg' ? 'text-2xl' : 'text-xl';

  return (
    <div className={`flex items-center gap-3 ${className}`}>
      {/* Shield & ID-card SVG icon */}
      <div className={`relative flex items-center justify-center rounded-xl bg-gradient-to-br from-primary to-navy text-white shadow-md ${iconSize} p-1.5 flex-shrink-0`}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-full h-full">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
          <rect x="7" y="8" width="10" height="7" rx="1.5" strokeWidth="1.5" />
          <circle cx="10" cy="11.5" r="1" fill="currentColor" stroke="none" />
          <line x1="13" y1="11" x2="15" y2="11" strokeWidth="1.5" />
          <line x1="13" y1="13" x2="15" y2="13" strokeWidth="1.5" />
        </svg>
      </div>

      <div className="leading-tight">
        <div className={`font-extrabold tracking-tight text-navy ${titleSize} flex items-center gap-1.5`}>
          <span>APAAR</span>
          <span className="text-primary font-bold">Connect</span>
          <span className="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-blue-100 text-primary font-semibold border border-blue-200">
            AICTE
          </span>
        </div>
        {showSubtitle && (
          <p className="text-[11px] text-slate-500 font-medium tracking-normal">
            Institutions Data & Verification Platform
          </p>
        )}
      </div>
    </div>
  );
};
