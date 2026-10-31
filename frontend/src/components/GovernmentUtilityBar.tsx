import React, { useEffect, useState } from 'react';
import { Accessibility, Minus, Plus, Sun } from 'lucide-react';

type TextScale = 'compact' | 'standard' | 'large';
const scales: TextScale[] = ['compact', 'standard', 'large'];

export const GovernmentUtilityBar: React.FC = () => {
  const [scale, setScale] = useState<TextScale>(() => {
    const saved = window.localStorage.getItem('apaar-ui-scale');
    return scales.includes(saved as TextScale) ? (saved as TextScale) : 'standard';
  });
  const [highContrast, setHighContrast] = useState(() => window.localStorage.getItem('apaar-high-contrast') === 'true');
  const [today] = useState(() => new Intl.DateTimeFormat('en-IN', {
    day: 'numeric', month: 'long', year: 'numeric', timeZone: 'Asia/Kolkata',
  }).format(new Date()));

  useEffect(() => {
    document.documentElement.dataset.textScale = scale;
    window.localStorage.setItem('apaar-ui-scale', scale);
  }, [scale]);
  useEffect(() => {
    document.documentElement.dataset.highContrast = String(highContrast);
    window.localStorage.setItem('apaar-high-contrast', String(highContrast));
  }, [highContrast]);

  const changeScale = (direction: -1 | 1) => setScale((current) =>
    scales[Math.max(0, Math.min(scales.length - 1, scales.indexOf(current) + direction))]);

  return (
    <>
      <a className="skip-to-content" href="#main-content">Skip to main content</a>
      <div className="govt-utility-bar no-print">
        <div className="govt-utility-inner">
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            <span className="font-semibold">APAAR GARUDA NETRA</span>
            <span className="hidden sm:inline text-slate-400" aria-hidden="true">|</span>
            <span className="text-slate-600">Digital student identity &amp; academic verification prototype</span>
          </div>
          <div className="flex items-center gap-3 sm:gap-4">
            <span className="hidden md:inline text-slate-600">{today}</span>
            <div className="flex items-center gap-1" role="group" aria-label="Text size controls">
              <span className="hidden sm:inline mr-1 text-slate-600">Text size</span>
              <button className="govt-utility-button" type="button" onClick={() => changeScale(-1)} aria-label="Decrease text size" title="Decrease text size"><Minus className="h-3.5 w-3.5" /></button>
              <button className="govt-utility-button" type="button" onClick={() => setScale('standard')} aria-label="Reset text size" title="Reset text size">A</button>
              <button className="govt-utility-button" type="button" onClick={() => changeScale(1)} aria-label="Increase text size" title="Increase text size"><Plus className="h-3.5 w-3.5" /></button>
            </div>
            <button className={`govt-utility-button inline-flex items-center gap-1.5 ${highContrast ? 'is-active' : ''}`} type="button" aria-pressed={highContrast} onClick={() => setHighContrast((value) => !value)} title="Toggle high contrast">
              {highContrast ? <Sun className="h-3.5 w-3.5" /> : <Accessibility className="h-3.5 w-3.5" />}
              <span className="hidden sm:inline">Contrast</span>
            </button>
          </div>
        </div>
      </div>
      <div className="govt-tricolour-rule" aria-hidden="true"><span /><span /><span /></div>
    </>
  );
};
