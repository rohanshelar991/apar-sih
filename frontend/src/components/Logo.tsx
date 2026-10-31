import React from 'react';

interface LogoProps {
  className?: string;
  size?: 'sm' | 'md' | 'lg';
  showSubtitle?: boolean;
}

export const Logo: React.FC<LogoProps> = ({ className = '', size = 'md', showSubtitle = true }) => {
  const iconSize = size === 'sm' ? 'w-9 h-9' : size === 'lg' ? 'w-14 h-14' : 'w-11 h-11';
  const titleSize = size === 'sm' ? 'text-[13px]' : size === 'lg' ? 'text-xl' : 'text-[17px]';

  return (
    <div role="img" aria-label="APAAR GARUDA NETRA — student identity and verification prototype" className={`garuda-logo flex items-center gap-3 min-w-0 ${className}`}>
      <img src="/brand/garuda-mark.svg" alt="" aria-hidden="true" className={`${iconSize} flex-shrink-0`} />
      <div className="min-w-0 leading-tight">
        <div className="flex items-center gap-2 mb-0.5">
          <span className="text-[9px] font-bold tracking-[0.25em] text-primary">APAAR</span>
          <span className="text-[8px] font-semibold tracking-[0.1em] text-slate-500 border-l border-slate-300 pl-2">PROTOTYPE</span>
        </div>
        <div className={`garuda-wordmark font-extrabold tracking-[0.04em] text-navy whitespace-nowrap ${titleSize}`}>
          GARUDA NETRA
        </div>
        {showSubtitle && (
          <p className="mt-1 text-[10px] text-slate-500 font-medium max-w-[19rem] leading-relaxed">
            Student identity &amp; academic verification prototype
          </p>
        )}
      </div>
    </div>
  );
};
